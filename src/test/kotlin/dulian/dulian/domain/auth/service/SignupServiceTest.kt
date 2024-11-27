package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.domain.auth.exception.SignupErrorCode
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder

class SignupServiceTest : BehaviorSpec({

    val memberRepository = mockk<MemberRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()

    val signupService = SignupService(memberRepository, passwordEncoder)

    Given("아이디가 중복일 때") {
        every { memberRepository.existsByUserId(any()) } returns true

        When("회원가입을 하면") {
            val request = SignupDto.Request(
                userId = "test",
                email = "test@test.com",
                emailConfirmCode = "1234",
                password = "1234",
                passwordConfirm = "1234",
                nickname = "test"
            )
            val exception = shouldThrow<CustomException> {
                signupService.signup(request)
            }

            Then("exception") {
                exception shouldBe CustomException(SignupErrorCode.EXISTED_USER_ID)

                verify { memberRepository.existsByUserId(request.userId) }
            }
        }
    }

    Given("닉네임이 중복일 때") {
        every { memberRepository.existsByUserId(any()) } returns false
        every { memberRepository.existsByNickname(any()) } returns true

        When("회원가입을 하면") {
            val request = SignupDto.Request(
                userId = "test",
                email = "test@test.com",
                emailConfirmCode = "1234",
                password = "1234",
                passwordConfirm = "1234",
                nickname = "test"
            )

            val exception = shouldThrow<CustomException> {
                signupService.signup(request)
            }
            Then("Exception") {
                exception shouldBe CustomException(SignupErrorCode.EXISTED_NICKNAME)

                verify { memberRepository.existsByUserId(request.userId) }
                verify { memberRepository.existsByNickname(request.nickname) }
            }
        }
    }

    Given("비밀번호가 일치하지 않을 때") {
        every { memberRepository.existsByUserId(any()) } returns false
        every { memberRepository.existsByNickname(any()) } returns false

        When("회원가입을 하면") {
            val request = SignupDto.Request(
                userId = "test",
                email = "test@test.com",
                emailConfirmCode = "1234",
                password = "1234",
                passwordConfirm = "12345",
                nickname = "test"
            )

            val exception = shouldThrow<CustomException> {
                signupService.signup(request)
            }

            Then("Exception") {
                exception shouldBe CustomException(SignupErrorCode.PASSWORD_CONFIRM_FAIL)

                verify { memberRepository.existsByUserId(request.userId) }
                verify { memberRepository.existsByNickname(request.nickname) }
            }
        }
    }

    Given("이메일 인증번호가 일치하지 않을 때") {
        When("회원가입을 하면") {
            Then("Exception") {

            }
        }
    }

    Given("정상적인 요청일 때") {
        every { memberRepository.existsByUserId(any()) } returns false
        every { memberRepository.existsByNickname(any()) } returns false
        every { passwordEncoder.encode(any()) } answers { firstArg() }
        every { memberRepository.save(any()) } answers { firstArg() }

        When("회원가입을 하면") {
            val request = SignupDto.Request(
                userId = "test",
                email = "test@test.com",
                emailConfirmCode = "1234",
                password = "1234",
                passwordConfirm = "1234",
                nickname = "test"
            )

            signupService.signup(request)


            Then("회원가입 성공") {
                verify { memberRepository.existsByUserId(request.userId) }
                verify { memberRepository.existsByNickname(request.nickname) }
                verify { passwordEncoder.encode(request.password) }
                verify { memberRepository.save(any()) }
            }
        }
    }
})