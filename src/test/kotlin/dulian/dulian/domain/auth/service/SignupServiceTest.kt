package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.exception.SignupErrorCode
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.mail.components.EmailUtils
import dulian.dulian.domain.mail.exception.EmailErrorCode
import dulian.dulian.domain.mail.repository.EmailCodeRepository
import dulian.dulian.fixtures.emailFixture
import dulian.dulian.fixtures.sendEmailConfirmCodeFixture
import dulian.dulian.fixtures.signupDtoFixture
import dulian.dulian.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.security.crypto.password.PasswordEncoder

class SignupServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val memberRepository = mockk<MemberRepository>()
    val emailCodeRepository = mockk<EmailCodeRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val emailUtils = mockk<EmailUtils>()

    val signupService = SignupService(
        memberRepository,
        emailCodeRepository,
        passwordEncoder,
        emailUtils
    )

    Context("회원가입") {
        Given("아이디가 중복일 때") {
            every { memberRepository.existsByUserId(any()) } returns true

            When("회원가입을 하면") {
                val request = signupDtoFixture()

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
                val request = signupDtoFixture()

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
                val request = signupDtoFixture("12345")

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
            every { memberRepository.existsByUserId(any()) } returns false
            every { memberRepository.existsByNickname(any()) } returns false
            every { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) } returns null

            When("회원가입을 하면") {
                val request = signupDtoFixture()

                val exception = shouldThrow<CustomException> {
                    signupService.signup(request)
                }

                Then("Exception") {
                    exception shouldBe CustomException(SignupErrorCode.INVALID_EMAIL_CODE)

                    verify { memberRepository.existsByUserId(request.userId) }
                    verify { memberRepository.existsByNickname(request.nickname) }
                    verify { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) }
                }
            }
        }

        Given("이메일 인증 시간초과일때") {
            every { memberRepository.existsByUserId(any()) } returns false
            every { memberRepository.existsByNickname(any()) } returns false
            every {
                emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(
                    any(),
                    any(),
                    any()
                )
            } returns emailFixture(4)

            When("회원가입을 하면") {
                val request = signupDtoFixture()

                val exception = shouldThrow<CustomException> {
                    signupService.signup(request)
                }

                Then("Exception") {
                    exception shouldBe CustomException(SignupErrorCode.INVALID_EMAIL_CODE)

                    verify { memberRepository.existsByUserId(request.userId) }
                    verify { memberRepository.existsByNickname(request.nickname) }
                    verify { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) }
                }
            }
        }

        Given("정상적인 요청일 때") {
            every { memberRepository.existsByUserId(any()) } returns false
            every { memberRepository.existsByNickname(any()) } returns false
            every { passwordEncoder.encode(any()) } answers { firstArg() }
            every { memberRepository.save(any()) } answers { firstArg() }
            every {
                emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(
                    any(),
                    any(),
                    any()
                )
            } returns emailFixture()

            When("회원가입을 하면") {
                val request = signupDtoFixture()

                signupService.signup(request)


                Then("회원가입 성공") {
                    verify { memberRepository.existsByUserId(request.userId) }
                    verify { memberRepository.existsByNickname(request.nickname) }
                    verify { passwordEncoder.encode(request.password) }
                    verify { memberRepository.save(any()) }
                    verify { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) }
                }
            }
        }
    }

    Context("이메일 인증 코드 전송") {
        Given("이메일이 중복일 때") {
            every { memberRepository.existsByEmail(any()) } returns true

            When("이메일 인증 코드 전송을 요청하면") {
                val exception = shouldThrow<CustomException> {
                    signupService.sendEmailConfirmCode(sendEmailConfirmCodeFixture())
                }

                Then("Exception") {
                    exception shouldBe CustomException(SignupErrorCode.EXISTED_EMAIL)

                    verify { memberRepository.existsByEmail(any()) }
                }
            }
        }

        Given("이메일 전송 시 오류가 발생할 때") {
            every { memberRepository.existsByEmail(any()) } returns false
            every { emailUtils.sendEmail(any()) } throws CustomException(EmailErrorCode.FAILED_SEND_EMAIL)

            When("이메일 인증 코드 전송을 요청하면") {
                val exception = shouldThrow<CustomException> {
                    signupService.sendEmailConfirmCode(sendEmailConfirmCodeFixture())
                }

                Then("Exception") {
                    exception shouldBe CustomException(EmailErrorCode.FAILED_SEND_EMAIL)

                    verify { memberRepository.existsByEmail(any()) }
                    verify { emailUtils.sendEmail(any()) }
                }
            }
        }

        Given("정상적인 요청일 때") {
            every { memberRepository.existsByEmail(any()) } returns false
            every { emailUtils.sendEmail(any()) } just Runs
            every { emailCodeRepository.save(any()) } answers { firstArg() }

            When("이메일 인증 코드 전송을 요청하면") {
                signupService.sendEmailConfirmCode(sendEmailConfirmCodeFixture())

                Then("이메일 인증 코드 전송 성공") {
                    verify { memberRepository.existsByEmail(any()) }
                    verify { emailUtils.sendEmail(any()) }
                    verify { emailCodeRepository.save(any()) }
                }
            }
        }
    }
})