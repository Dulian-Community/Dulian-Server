package dulian.dulian.domain.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
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
    val mockMvc = ControllerTestUtils.initMockMvc(context, restDocumentation)
    val fixtureMonkey = fixtureMonkey()

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

        context("정상적인 요청이면") {
            every { signupService.signup(request) } just Runs

            it("회원가입 성공") {
                mockMvc.makeDocument("회원가입 성공", "Auth", "회원가입 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/signup") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    requestBody {
                        field("userId", "아이디")
                        field("email", "이메일")
                        field("emailConfirmCode", "이메일 인증번호")
                        field("password", "비밀번호")
                        field("passwordConfirm", "비밀번호 확인")
                        field("nickname", "닉네임")
                    }
                }

                verify { signupService.signup(request) }
            }
        }

        context("아이디가 중복인 경우") {
            every { signupService.signup(request) } throws CustomException(SignupErrorCode.EXISTED_USER_ID)

            it("회원가입 실패") {
                mockMvc.makeDocument("회원가입 실패 - 아이디 중복", "Auth", "회원가입 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/signup") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", SignupErrorCode.EXISTED_USER_ID.message)
                    }
                }

                verify { signupService.signup(request) }
            }
        }

        context("닉네임이 중복인 경우") {
            every { signupService.signup(request) } throws CustomException(SignupErrorCode.EXISTED_NICKNAME)

            it("회원가입 실패") {
                mockMvc.makeDocument("회원가입 실패 - 닉네임 중복", "Auth", "회원가입 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/signup") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", SignupErrorCode.EXISTED_NICKNAME.message)
                    }
                }

                verify { signupService.signup(request) }
            }
        }

        context("비밀번호가 불일치하는 경우") {
            every { signupService.signup(request) } throws CustomException(SignupErrorCode.EXISTED_NICKNAME)

            it("회원가입 실패") {
                mockMvc.makeDocument("회원가입 실패 - 비밀번호 불일치", "Auth", "회원가입 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/signup") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", SignupErrorCode.EXISTED_NICKNAME.message)
                    }
                }

                verify { signupService.signup(request) }
            }
        }

        context("이메일 인증에 실패한 경우") {
            every { signupService.signup(request) } throws CustomException(SignupErrorCode.INVALID_EMAIL_CODE)

            it("회원가입 실패") {
                mockMvc.makeDocument("회원가입 실패 - 이메일 인증 실패", "Auth", "회원가입 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/signup") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", SignupErrorCode.INVALID_EMAIL_CODE.message)
                    }
                }

                verify { signupService.signup(request) }
            }
        }
    }

    describe("이메일 인증 코드 전송 API") {
        val request = fixtureMonkey.giveMeOne(SignupConfirmDto.Request::class.java)

        context("정상적인 요쳥인 경우") {
            every { signupService.sendEmailConfirmCode(request) } just Runs

            it("이메일 인증 코드 전송 성공") {
                mockMvc.makeDocument("이메일 인증 코드 전송 성공", "Auth", "이메일 인증 코드 전송 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/signup/send-email-confirm-code") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    requestBody {
                        field("email", "이메일")
                    }
                }

                verify { signupService.sendEmailConfirmCode(request) }
            }
        }

        context("이미 가입된 이메일인 경우") {
            every { signupService.sendEmailConfirmCode(request) } throws CustomException(SignupErrorCode.EXISTED_EMAIL)

            it("이메일 인증 코드 전송 실패") {
                mockMvc.makeDocument("이메일 인증 코드 전송 실패 - 이미 가입된 이메일", "Auth", "이메일 인증 코드 전송 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/signup/send-email-confirm-code") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", SignupErrorCode.EXISTED_EMAIL.message)
                    }
                }

                verify { signupService.sendEmailConfirmCode(request) }
            }
        }
    }

    describe("로그인 API") {
        val request = fixtureMonkey.giveMeOne(LoginDto.Request::class.java)
        val token = fixtureMonkey.giveMeOne(TokenDto.Token::class.java)

        context("ID가 존재 X or 비밀번호 불알치 or 소셜 로그인 사용자인 경우") {
            every { loginService.login(eq(request), any()) } throws CustomException(LoginErrorCode.FAILED_TO_LOGIN)

            it("로그인 실패") {
                mockMvc.makeDocument("로그인 실패 - ID 존재 X or 비밀번호 불일치 or 소셜 로그인 사용자", "Auth", "로그인 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/login") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", LoginErrorCode.FAILED_TO_LOGIN.message)
                    }
                    requestBody {
                        field("userId", "아이디")
                        field("password", "비밀번호")
                    }
                }

                verify { loginService.login(request, any()) }
            }
        }

        context("정상적인 경우") {
            every { loginService.login(eq(request), any()) } returns token

            it("로그인 성공") {
                mockMvc.makeDocument("로그인 성공", "Auth", "로그인 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/login") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder {
                        assert("data.token", token.token)
                        assert("data.expiresIn", token.expiresIn)
                    }
                    requestBody {
                        field("userId", "아이디")
                        field("password", "비밀번호")
                    }
                    responseBody {
                        field("data.token", "Access Token")
                        field("data.expiresIn", "Access Token 만료 시간")
                        field("status", "상태")
                        field("statusCode", "상태 코드")
                        field("timestamp", "응답 시간")
                    }
                }

                verify { loginService.login(request, any()) }
            }
        }
    }

    describe("Access Token 갱신 API") {
        val token = fixtureMonkey.giveMeOne(TokenDto.Token::class.java)

        context("Refresh Token이 존재하지 않는 경우") {
            every { tokenRefreshService.refresh(any()) } throws CustomException(RefreshTokenErrorCode.INVALID_REFRESH_TOKEN)

            it("Access Token 갱신 실패") {
                mockMvc.makeDocument("Access Token 갱신 실패 - Refresh Token 존재 X", "Auth", "Access Token 갱신 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/refresh") {}
                    assertBuilder(status().isUnauthorized) {
                        assert("message", RefreshTokenErrorCode.INVALID_REFRESH_TOKEN.message)
                    }
                }
            }
        }

        context("정상적인 경우") {
            every { tokenRefreshService.refresh(any()) } returns token

            it("Access Token 갱신 성공") {
                mockMvc.makeDocument("Access Token 갱신 성공", "Auth", "Access Token 갱신 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/refresh") {}
                    assertBuilder {
                        assert("data.token", token.token)
                        assert("data.expiresIn", token.expiresIn)
                    }
                    responseBody {
                        field("data.token", "Access Token")
                        field("data.expiresIn", "Access Token 만료 시간")
                        field("status", "상태")
                        field("statusCode", "상태 코드")
                        field("timestamp", "응답 시간")
                    }
                }
            }
        }
    }

    describe("로그아웃 API") {
        context("정상적인 요청인 경우") {
            every { logoutService.logout(any()) } just Runs

            it("로그아웃 성공") {
                mockMvc.makeDocument("로그아웃 성공", "Auth", "로그아웃 API") {
                    requestLine(HttpMethod.POST, "/api/v1/auth/logout") {}
                }
            }
        }
    }
})
