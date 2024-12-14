package dulian.dulian.domain.auth.service

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import dulian.dulian.domain.auth.dto.FindIdDto
import dulian.dulian.domain.auth.dto.FindPasswordDto
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.exception.AccountFindErrorCode
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.mail.components.EmailUtils
import dulian.dulian.domain.mail.entity.EmailCode
import dulian.dulian.domain.mail.repository.EmailCodeRepository
import dulian.dulian.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime

class AccountFindServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val memberRepository: MemberRepository = mockk()
    val emailCodeRepository: EmailCodeRepository = mockk()
    val emailUtils: EmailUtils = mockk()
    val passwordEncoder: PasswordEncoder = mockk()

    val accountFindService = AccountFindService(
        memberRepository,
        emailCodeRepository,
        emailUtils,
        passwordEncoder
    )

    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JakartaValidationPlugin())
        .build()

    Context("아이디 찾기 - Step1") {
        val request = fixtureMonkey.giveMeOne(FindIdDto.Step1Request::class.java)

        Given("이메일이 존재하지 않는 경우") {
            every { memberRepository.existsByEmail(any()) } returns false

            When("아이디 찾기를 요청하면") {
                val exception = shouldThrow<CustomException> {
                    accountFindService.findIdStep1(request)
                }

                Then("exception") {
                    exception shouldBe CustomException(AccountFindErrorCode.NOT_EXISTED_EMAIL)

                    verify { memberRepository.existsByEmail(any()) }
                }
            }
        }

        Given("정상적인 경우") {
            every { memberRepository.existsByEmail(any()) } returns true
            every { emailUtils.sendEmail(any()) } just Runs
            every { emailCodeRepository.save(any()) } returns mockk()

            When("아이디 찾기를 요청하면") {
                accountFindService.findIdStep1(request)

                Then("아이디 찾기 - Step1 성공") {
                    verify { memberRepository.existsByEmail(any()) }
                    verify { emailUtils.sendEmail(any()) }
                    verify { emailCodeRepository.save(any()) }
                }
            }
        }
    }

    Context("아이디 찾기 - Step2") {
        val request = fixtureMonkey.giveMeOne(FindIdDto.Step2Request::class.java)
        val emailCode = fixtureMonkey.giveMeBuilder(EmailCode::class.java)
            .set("createdAt", LocalDateTime.now())
            .sample()
        val timeoutEmailCode = fixtureMonkey.giveMeBuilder(EmailCode::class.java)
            .set("createdAt", LocalDateTime.now().minusMinutes(4))
            .sample()
        val member = fixtureMonkey.giveMeOne(Member::class.java)

        Given("이메일 코드 인증에 실패한 경우") {
            every { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) } returns null

            When("아이디 찾기를 요청하면") {
                val exception = shouldThrow<CustomException> {
                    accountFindService.findIdStep2(request)
                }

                Then("exception") {
                    exception shouldBe CustomException(AccountFindErrorCode.INVALID_EMAIL_CODE)

                    verify { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) }
                }
            }
        }

        Given("이메일 코드 인증 유효 시간이 초과된 경우") {
            every {
                emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any())
            } returns timeoutEmailCode

            When("아이디 찾기를 요청하면") {
                val exception = shouldThrow<CustomException> {
                    accountFindService.findIdStep2(request)
                }

                Then("exception") {
                    exception shouldBe CustomException(AccountFindErrorCode.INVALID_EMAIL_CODE)

                    verify { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) }
                }
            }
        }

        Given("아이디가 존재하지 않는 경우") {
            every { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) } returns emailCode
            every { memberRepository.findByEmail(any()) } returns null

            When("아이디 찾기를 요청하면") {
                val exception = shouldThrow<CustomException> {
                    accountFindService.findIdStep2(request)
                }

                Then("exception") {
                    exception shouldBe CustomException(AccountFindErrorCode.NOT_EXISTED_EMAIL)

                    verify { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) }
                    verify { memberRepository.findByEmail(request.email) }
                }
            }
        }

        Given("정상적인 경우") {
            every { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) } returns emailCode
            every { memberRepository.findByEmail(any()) } returns member

            When("아이디 찾기를 요청하면") {
                val result = accountFindService.findIdStep2(request)

                Then("아이디 찾기 - Step2 성공") {
                    result shouldBe member.userId

                    verify { emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(any(), any(), any()) }
                    verify { memberRepository.findByEmail(request.email) }
                }
            }
        }
    }

    Context("비밀번호 초기화") {
        val request = fixtureMonkey.giveMeOne(FindPasswordDto.Request::class.java)
        val member = fixtureMonkey.giveMeOne(Member::class.java)

        Given("계정 조회에 실패한 경우") {
            every { memberRepository.findByEmailAndUserId(any(), any()) } returns null

            When("비밀번호 초기화를 요청하면") {
                val exception = shouldThrow<CustomException> {
                    accountFindService.resetPassword(request)
                }

                Then("Exception") {
                    exception shouldBe CustomException(AccountFindErrorCode.INVALID_EMAIL_OR_USER_ID)

                    verify { memberRepository.findByEmailAndUserId(any(), any()) }
                }
            }
        }

        Given("정상적인 경우") {
            every { memberRepository.findByEmailAndUserId(any(), any()) } returns member
            every { passwordEncoder.encode(any()) } returns "encodedPassword"
            every { emailUtils.sendEmail(any()) } just Runs

            When("비밀번호 초기화를 요청하면") {
                accountFindService.resetPassword(request)

                Then("비밀번호 초기화 성공") {
                    verify { memberRepository.findByEmailAndUserId(any(), any()) }
                    verify { emailUtils.sendEmail(any()) }
                }
            }
        }
    }
})
