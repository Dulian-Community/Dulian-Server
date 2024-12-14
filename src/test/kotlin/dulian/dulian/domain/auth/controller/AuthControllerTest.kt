package dulian.dulian.domain.auth.controller

import com.epages.restdocs.apispec.ConstrainedFields
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.auth.dto.LoginDto
import dulian.dulian.domain.auth.dto.SignupConfirmDto
import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.domain.auth.exception.LoginErrorCode
import dulian.dulian.domain.auth.exception.RefreshTokenErrorCode
import dulian.dulian.domain.auth.exception.SignupErrorCode
import dulian.dulian.domain.auth.service.LoginService
import dulian.dulian.domain.auth.service.LogoutService
import dulian.dulian.domain.auth.service.SignupService
import dulian.dulian.domain.auth.service.TokenRefreshService
import dulian.dulian.global.auth.jwt.dto.TokenDto
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
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
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

    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JakartaValidationPlugin())
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
                                    .tag("Auth")
                                    .summary("회원가입 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { signupService.signup(request) }
            }
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
                                    .tag("Auth")
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
                                    .tag("Auth")
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
                                    .tag("Auth")
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
                            "실패 - 이메일 인증 실패",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
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

    describe("이메일 인증 코드 전송 API") {
        val request = fixtureMonkey.giveMeOne(SignupConfirmDto.Request::class.java)
        val fields = ConstrainedFields(SignupConfirmDto.Request::class.java)

        val fieldDescriptors = listOf(
            fields.withPath("email").description("이메일")
        )

        context("정상적인 요쳥인 경우") {
            every { signupService.sendEmailConfirmCode(request) } just Runs

            it("이메일 인증 코드 전송 성공") {
                mockMvc.perform(
                    post("/api/v1/auth/signup/send-email-confirm-code")
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
                                    .tag("Auth")
                                    .summary("이메일 인증 코드 전송 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { signupService.sendEmailConfirmCode(request) }
            }
        }

        context("이미 가입된 이메일인 경우") {
            every { signupService.sendEmailConfirmCode(request) } throws CustomException(SignupErrorCode.EXISTED_EMAIL)

            it("이메일 인증 코드 전송 실패") {
                mockMvc.perform(
                    post("/api/v1/auth/signup/send-email-confirm-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        document(
                            "실패 - 이미 가입된 이메일",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("이메일 인증 코드 전송 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { signupService.sendEmailConfirmCode(request) }
            }
        }
    }

    describe("로그인 API") {
        val request = fixtureMonkey.giveMeOne(LoginDto.Request::class.java)
        val token = fixtureMonkey.giveMeOne(TokenDto.Token::class.java)

        val fields = ConstrainedFields(LoginDto.Request::class.java)

        val fieldDescriptors = listOf(
            fields.withPath("userId").description("아이디"),
            fields.withPath("password").description("비밀번호")
        )

        context("ID가 존재 X or 비밀번호 불알치 or 소셜 로그인 사용자인 경우") {
            println(request)
            every { loginService.login(eq(request), any()) } throws CustomException(LoginErrorCode.FAILED_TO_LOGIN)

            it("로그인 실패") {
                mockMvc.perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        document(
                            "실패 - ID 존재 X or 비밀번호 불일치 or 소셜 로그인 사용자",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("로그인 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { loginService.login(request, any()) }
            }
        }

        context("정상적인 경우") {
            println(request)
            every { loginService.login(eq(request), any()) } returns token

            it("로그인 성공") {
                mockMvc.perform(
                    post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.data.token").value(token.token))
                    .andExpect(jsonPath("$.data.expiresIn").value(token.expiresIn))
                    .andDo(
                        document(
                            "성공",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("로그인 API")
                                    .requestFields(fieldDescriptors)
                                    .responseFields(
                                        fields.withPath("status").description("상태"),
                                        fields.withPath("statusCode").description("상태 코드"),
                                        fields.withPath("timestamp").description("응답 시간"),
                                        fields.withPath("data.token").description("Access Token"),
                                        fields.withPath("data.expiresIn").description("Access Token 만료 시간")
                                    )
                                    .build()
                            )
                        )
                    )

                verify { loginService.login(request, any()) }
            }
        }
    }

    describe("Access Token 갱신 API") {
        val token = fixtureMonkey.giveMeOne(TokenDto.Token::class.java)

        context("Refresh Token이 존재하지 않는 경우") {
            every { tokenRefreshService.refresh(any()) } throws CustomException(RefreshTokenErrorCode.INVALID_REFRESH_TOKEN)

            it("Access Token 갱신 실패") {
                mockMvc.perform(
                    post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isUnauthorized)
                    .andDo(
                        document(
                            "실패 - Refresh Token 존재 X",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("Access Token 갱신 API")
                                    .build()
                            )
                        )
                    )
            }
        }

        context("정상적인 경우") {
            every { tokenRefreshService.refresh(any()) } returns token

            it("Access Token 갱신 성공") {
                mockMvc.perform(
                    post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("$.data.token").value(token.token))
                    .andExpect(jsonPath("$.data.expiresIn").value(token.expiresIn))
                    .andDo(
                        document(
                            "성공",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("Access Token 갱신 API")
                                    .responseFields(
                                        fieldWithPath("status").description("상태"),
                                        fieldWithPath("statusCode").description("상태 코드"),
                                        fieldWithPath("timestamp").description("응답 시간"),
                                        fieldWithPath("data.token").description("Access Token"),
                                        fieldWithPath("data.expiresIn").description("Access Token 만료 시간")
                                    )
                                    .build()
                            )
                        )
                    )
            }
        }
    }

    describe("로그아웃 API") {
        context("정상적인 요청인 경우") {
            every { logoutService.logout(any()) } just Runs

            it("로그아웃 성공") {
                mockMvc.perform(
                    post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            "성공",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("로그아웃 API")
                                    .build()
                            )
                        )
                    )
            }
        }
    }
})
