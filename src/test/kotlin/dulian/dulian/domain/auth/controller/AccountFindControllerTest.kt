package dulian.dulian.domain.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.auth.dto.FindIdDto
import dulian.dulian.domain.auth.dto.FindPasswordDto
import dulian.dulian.domain.auth.exception.AccountFindErrorCode
import dulian.dulian.domain.auth.service.AccountFindService
import dulian.dulian.global.exception.CustomException
import dulian.dulian.utils.ControllerTestUtils
import dulian.dulian.utils.fixtureMonkey
import dulian.dulian.utils.makeDocument
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationExtension
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

        context("이메일이 존재하지 않는 경우") {
            every { accountFindService.findIdStep1(any()) } throws CustomException(AccountFindErrorCode.NOT_EXISTED_EMAIL)

            it("실패") {
                mockMvc.makeDocument("아이디 찾기 - 이메일 전송 실패 - 이메일이 존재하지 않는 경우", "Auth", "아이디 찾기 - 이메일 전송 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/find-id/step1") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }

                    assertBuilder(status().isBadRequest) {
                        assert("message", AccountFindErrorCode.NOT_EXISTED_EMAIL.message)
                    }
                }

                verify { accountFindService.findIdStep1(any()) }
            }
        }

        context("정상적인 요청인 경우") {
            every { accountFindService.findIdStep1(any()) } just Runs

            it("성공") {
                mockMvc.makeDocument("아이디 찾기 - 이메일 전송 성공", "Auth", "아이디 찾기 - 이메일 전송 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/find-id/step1") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }

                    requestBody {
                        field("email", "이메일")
                    }
                }

                verify { accountFindService.findIdStep1(any()) }
            }
        }
    }

    describe("아이디 찾기 - 인증 코드 검증 API") {
        val request = fixtureMonkey.giveMeOne(FindIdDto.Step2Request::class.java)

        context("올바르지 않은 인증 코드인 경우") {
            every { accountFindService.findIdStep2(any()) } throws CustomException(AccountFindErrorCode.INVALID_EMAIL_CODE)

            it("실패") {
                mockMvc.makeDocument("아이디 찾기 - 인증 코드 검증 실패 - 올바르지 않은 인증 코드인 경우", "Auth", "아이디 찾기 - 인증 코드 검증 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/find-id/step2") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", AccountFindErrorCode.INVALID_EMAIL_CODE.message)
                    }
                }

                verify { accountFindService.findIdStep2(any()) }
            }
        }

        context("존재하지 않는 이메일인 경우") {
            every { accountFindService.findIdStep2(any()) } throws CustomException(AccountFindErrorCode.NOT_EXISTED_EMAIL)

            it("실패") {
                mockMvc.makeDocument("아이디 찾기 - 인증 코드 검증 실패 - 존재하지 않는 이메일인 경우", "Auth", "아이디 찾기 - 인증 코드 검증 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/find-id/step2") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", AccountFindErrorCode.NOT_EXISTED_EMAIL.message)
                    }
                }

                verify { accountFindService.findIdStep2(any()) }
            }
        }

        context("정상적인 요청인 경우") {
            every { accountFindService.findIdStep2(any()) } returns "userId"

            it("성공") {
                mockMvc.makeDocument("아이디 찾기 - 인증 코드 검증 성공", "Auth", "아이디 찾기 - 인증 코드 검증 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/find-id/step2") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    requestBody {
                        field("email", "이메일")
                        field("code", "인증 코드")
                    }
                    responseBody {
                        field("data.userId", "아이디")
                        field("status", "상태")
                        field("statusCode", "상태 코드")
                        field("timestamp", "응답 시간")
                    }
                }

                verify { accountFindService.findIdStep2(any()) }
            }
        }
    }

    describe("비밀번호 재설정 API") {
        val request = fixtureMonkey.giveMeOne(FindPasswordDto.Request::class.java)

        context("계정이 존재하지 않는 경우") {
            every { accountFindService.resetPassword(any()) } throws CustomException(AccountFindErrorCode.INVALID_EMAIL_OR_USER_ID)

            it("실패") {
                mockMvc.makeDocument("비밀번호 재설정 실패 - 계정이 존재하지 않는 경우", "Auth", "비밀번호 재설정 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/reset-password") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }

                    assertBuilder(status().isBadRequest) {
                        assert("message", AccountFindErrorCode.INVALID_EMAIL_OR_USER_ID.message)
                    }
                }

                verify { accountFindService.resetPassword(any()) }
            }
        }

        context("정상적인 요청인 경우") {
            every { accountFindService.resetPassword(any()) } just Runs

            it("성공") {
                mockMvc.makeDocument("비밀번호 재설정 성공", "Auth", "비밀번호 재설정 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/reset-password") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    requestBody {
                        field("email", "이메일")
                        field("userId", "아이디")
                    }
                }

                verify { accountFindService.resetPassword(any()) }
            }
        }
    }
})
