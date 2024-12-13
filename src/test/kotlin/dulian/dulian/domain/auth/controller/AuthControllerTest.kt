package dulian.dulian.domain.auth.controller

import com.epages.restdocs.apispec.ConstrainedFields
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.domain.auth.exception.SignupErrorCode
import dulian.dulian.domain.auth.service.LoginService
import dulian.dulian.domain.auth.service.LogoutService
import dulian.dulian.domain.auth.service.SignupService
import dulian.dulian.domain.auth.service.TokenRefreshService
import dulian.dulian.global.exception.CustomException
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

@WebMvcTest(AuthController::class)
@ExtendWith(RestDocumentationExtension::class)
class AuthControllerTest(
    @MockkBean
    private val signupService: SignupService,

    @MockkBean
    private val loginService: LoginService,

    @MockkBean
    private val tokenRefreshService: TokenRefreshService,

    @MockkBean
    private val logoutService: LogoutService,

    @Autowired
    private val context: WebApplicationContext,

    @Autowired
    private val objectMapper: ObjectMapper
) : DescribeSpec({

    val restDocumentation = ManualRestDocumentation()
    val mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply<DefaultMockMvcBuilder>(
            MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(Preprocessors.prettyPrint())
                .withResponseDefaults(Preprocessors.prettyPrint())
        )
        .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
        .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
        .build()


    beforeEach { restDocumentation.beforeTest(javaClass, it.name.testName) }
    afterEach { restDocumentation.afterTest() }

    describe("회원가입 API") {
        val request = SignupDto.Request(
            userId = "test12345",
            email = "test@test.com",
            emailConfirmCode = "1234",
            password = "Test1234!",
            passwordConfirm = "Test1234!",
            nickname = "test"
        )
        val fields = ConstrainedFields(SignupDto.Request::class.java)

        val fieldDescriptors = listOf(
            fields.withPath("userId").description("아이디"),
            fields.withPath("email").description("이메일"),
            fields.withPath("emailConfirmCode").description("이메일 인증번호"),
            fields.withPath("password").description("비밀번호"),
            fields.withPath("passwordConfirm").description("비밀번호 확인"),
            fields.withPath("nickname").description("닉네임")
        )

        context("정상적인 요청이면") {
            every { signupService.signup(request) } just Runs

            it("회원가입 성공") {

                mockMvc.perform(
                    post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            "성공",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("회원가입")
                                    .summary("회원가입 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { signupService.signup(request) }
            }

            context("아이디가 중복인 경우") {
                every { signupService.signup(request) } throws CustomException(SignupErrorCode.EXISTED_USER_ID)

                it("회원가입 실패") {
                    mockMvc.perform(
                        post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            document(
                                "실패 - 아이디 중복",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                resource(
                                    ResourceSnippetParameters.builder()
                                        .tag("회원가입")
                                        .summary("회원가입 API")
                                        .requestFields(fieldDescriptors)
                                        .build()
                                )
                            )
                        )

                    verify { signupService.signup(request) }
                }
            }

            context("닉네임이 중복인 경우") {
                every { signupService.signup(request) } throws CustomException(SignupErrorCode.EXISTED_NICKNAME)

                it("회원가입 실패") {
                    mockMvc.perform(
                        post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            document(
                                "실패 - 닉네임 중복",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                resource(
                                    ResourceSnippetParameters.builder()
                                        .tag("회원가입")
                                        .summary("회원가입 API")
                                        .requestFields(fieldDescriptors)
                                        .build()
                                )
                            )
                        )

                    verify { signupService.signup(request) }
                }
            }

            context("비밀번호가 불일치하는 경우") {
                every { signupService.signup(request) } throws CustomException(SignupErrorCode.EXISTED_NICKNAME)

                it("회원가입 실패") {
                    mockMvc.perform(
                        post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            document(
                                "실패 - 비밀번호 불일치",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                resource(
                                    ResourceSnippetParameters.builder()
                                        .tag("회원가입")
                                        .summary("회원가입 API")
                                        .requestFields(fieldDescriptors)
                                        .build()
                                )
                            )
                        )

                    verify { signupService.signup(request) }
                }
            }

            context("이메일 인증에 실패한 경우") {
                every { signupService.signup(request) } throws CustomException(SignupErrorCode.INVALID_EMAIL_CODE)

                it("회원가입 실패") {
                    mockMvc.perform(
                        post("/api/v1/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                    )
                        .andExpect(status().isBadRequest)
                        .andDo(
                            document(
                                "실패 - 비밀번호 불일치",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                resource(
                                    ResourceSnippetParameters.builder()
                                        .tag("회원가입")
                                        .summary("회원가입 API")
                                        .requestFields(fieldDescriptors)
                                        .build()
                                )
                            )
                        )

                    verify { signupService.signup(request) }
                }
            }
        }
    }
})
