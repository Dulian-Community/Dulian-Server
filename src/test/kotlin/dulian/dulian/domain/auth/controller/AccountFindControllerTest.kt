package dulian.dulian.domain.auth.controller

import com.epages.restdocs.apispec.ConstrainedFields
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.auth.dto.FindIdDto
import dulian.dulian.domain.auth.dto.FindPasswordDto
import dulian.dulian.domain.auth.exception.AccountFindErrorCode
import dulian.dulian.domain.auth.service.AccountFindService
import dulian.dulian.global.exception.CustomException
import dulian.dulian.utils.ControllerTestUtils
import dulian.dulian.utils.fixtureMonkey
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
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(AccountFindController::class)
@ExtendWith(RestDocumentationExtension::class)
class AccountFindControllerTest(
    @MockkBean
    private val accountFindService: AccountFindService,

    @Autowired
    private val context: WebApplicationContext,

    @Autowired
    private val objectMapper: ObjectMapper
) : DescribeSpec({
    val restDocumentation = ManualRestDocumentation()
    val mockMvc = ControllerTestUtils.initMockMvc(context, restDocumentation)
    val fixtureMonkey = fixtureMonkey()

    beforeEach { restDocumentation.beforeTest(javaClass, it.name.testName) }
    afterEach { restDocumentation.afterTest() }

    describe("아이디 찾기 - 이메일 전송 API") {
        val request = fixtureMonkey.giveMeOne(FindIdDto.Step1Request::class.java)
        val fields = ConstrainedFields(FindIdDto.Step1Request::class.java)
        val fieldDescriptors = listOf(
            fields.withPath("email").description("이메일")
        )

        context("이메일이 존재하지 않는 경우") {
            every { accountFindService.findIdStep1(any()) } throws CustomException(AccountFindErrorCode.NOT_EXISTED_EMAIL)

            it("실패") {
                mockMvc.perform(
                    post("/api/v1/auth/find-id/step1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        document(
                            "실패 - 이메일이 존재하지 않는 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("아이디 찾기 - 이메일 전송 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { accountFindService.findIdStep1(any()) }
            }
        }

        context("정상적인 요청인 경우") {
            every { accountFindService.findIdStep1(any()) } just Runs

            it("성공") {
                mockMvc.perform(
                    post("/api/v1/auth/find-id/step1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            "성공 - 정상적인 요청인 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("아이디 찾기 - 이메일 전송 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { accountFindService.findIdStep1(any()) }
            }
        }
    }

    describe("아이디 찾기 - 인증 코드 검증 API") {
        val request = fixtureMonkey.giveMeOne(FindIdDto.Step2Request::class.java)
        val fields = ConstrainedFields(FindIdDto.Step2Request::class.java)
        val fieldDescriptors = listOf(
            fields.withPath("email").description("이메일"),
            fields.withPath("code").description("인증 코드")
        )

        context("올바르지 않은 인증 코드인 경우") {
            every { accountFindService.findIdStep2(any()) } throws CustomException(AccountFindErrorCode.INVALID_EMAIL_CODE)

            it("실패") {
                mockMvc.perform(
                    post("/api/v1/auth/find-id/step2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        document(
                            "실패 - 올바르지 않은 인증 코드인 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("아이디 찾기 - 인증 코드 검증 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { accountFindService.findIdStep2(any()) }
            }
        }

        context("존재하지 않는 이메일인 경우") {
            every { accountFindService.findIdStep2(any()) } throws CustomException(AccountFindErrorCode.NOT_EXISTED_EMAIL)

            it("실패") {
                mockMvc.perform(
                    post("/api/v1/auth/find-id/step2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        document(
                            "실패 - 존재하지 않는 이메일인 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("아이디 찾기 - 인증 코드 검증 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { accountFindService.findIdStep2(any()) }
            }
        }

        context("정상적인 요청인 경우") {
            every { accountFindService.findIdStep2(any()) } returns "userId"

            it("성공") {
                mockMvc.perform(
                    post("/api/v1/auth/find-id/step2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            "성공 - 정상적인 요청인 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("아이디 찾기 - 인증 코드 검증 API")
                                    .requestFields(fieldDescriptors)
                                    .responseFields(
                                        fieldWithPath("status").description("상태"),
                                        fieldWithPath("statusCode").description("상태 코드"),
                                        fieldWithPath("timestamp").description("응답 시간"),
                                        fieldWithPath("data.userId").description("아이디")
                                    )
                                    .build()
                            )
                        )
                    )

                verify { accountFindService.findIdStep2(any()) }
            }
        }
    }

    describe("비밀번호 재설정 API") {
        val request = fixtureMonkey.giveMeOne(FindPasswordDto.Request::class.java)
        val fields = ConstrainedFields(FindIdDto.Step2Request::class.java)
        val fieldDescriptors = listOf(
            fields.withPath("email").description("이메일"),
            fields.withPath("userId").description("아이디")
        )

        context("계정이 존재하지 않는 경우") {
            every { accountFindService.resetPassword(any()) } throws CustomException(AccountFindErrorCode.INVALID_EMAIL_OR_USER_ID)

            it("실패") {
                mockMvc.perform(
                    post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isBadRequest)
                    .andDo(
                        document(
                            "실패 - 계정이 존재하지 않는 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("비밀번호 재설정 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { accountFindService.resetPassword(any()) }
            }
        }

        context("정상적인 요청인 경우") {
            every { accountFindService.resetPassword(any()) } just Runs

            it("성공") {
                mockMvc.perform(
                    post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            "성공 - 정상적인 요청인 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("Auth")
                                    .summary("비밀번호 재설정 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )

                verify { accountFindService.resetPassword(any()) }
            }
        }
    }
})
