package dulian.dulian.domain.auth.service

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import dulian.dulian.domain.auth.dto.LoginDto
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.exception.LoginErrorCode
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.auth.repository.RefreshTokenRepository
import dulian.dulian.global.auth.enums.SocialType
import dulian.dulian.global.auth.jwt.components.JwtTokenProvider
import dulian.dulian.global.auth.jwt.dto.TokenDto
import dulian.dulian.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.crypto.password.PasswordEncoder

class LoginServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val memberRepository: MemberRepository = mockk()
    val passwordEncoder: PasswordEncoder = mockk()
    val jwtTokenProvider: JwtTokenProvider = mockk()
    val refreshTokenRepository: RefreshTokenRepository = mockk()

    val loginService = LoginService(
        memberRepository,
        passwordEncoder,
        jwtTokenProvider,
        refreshTokenRepository
    )

    val monkeyFixture = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JakartaValidationPlugin())
        .build()

    Context("로그인") {
        val request = monkeyFixture.giveMeBuilder(LoginDto.Request::class.java)
            .set("socialType", null)
            .sample()
        val member = monkeyFixture.giveMeBuilder(Member::class.java)
            .set("memberId", 1L)
            .set("socialType", null)
            .sample()
        val socialMember = monkeyFixture.giveMeBuilder(Member::class.java)
            .set("socialType", SocialType.NAVER)
            .sample()
        val tokenDto = monkeyFixture.giveMeBuilder(TokenDto::class.java)
            .set("refreshToken.token", "token")
            .sample()
        val response = MockHttpServletResponse()

        Given("ID가 존재하지 않을 때") {
            every { memberRepository.findByUserId(request.userId) } returns null

            When("로그인을 하면") {
                val exception = shouldThrow<CustomException> {
                    loginService.login(request, response)
                }

                Then("exception") {
                    exception shouldBe CustomException(LoginErrorCode.FAILED_TO_LOGIN)

                    verify { memberRepository.findByUserId(request.userId) }
                }
            }
        }

        Given("비밀번호가 일치하지 않을 때") {
            every { memberRepository.findByUserId(request.userId) } returns member
            every { passwordEncoder.matches(request.password, member.password) } returns false

            When("로그인을 하면") {
                val exception = shouldThrow<CustomException> {
                    loginService.login(request, response)
                }

                Then("exception") {
                    exception shouldBe CustomException(LoginErrorCode.FAILED_TO_LOGIN)

                    verify { memberRepository.findByUserId(request.userId) }
                    verify { passwordEncoder.matches(request.password, member.password) }
                }
            }
        }

        Given("소셜 로그인 사용자인 경우") {
            every { memberRepository.findByUserId(request.userId) } returns socialMember
            every { passwordEncoder.matches(request.password, socialMember.password) } returns true

            When("로그인을 하면") {
                val exception = shouldThrow<CustomException> {
                    loginService.login(request, response)
                }

                Then("exception") {
                    exception shouldBe CustomException(LoginErrorCode.FAILED_TO_LOGIN)

                    verify { memberRepository.findByUserId(request.userId) }
                    verify { passwordEncoder.matches(request.password, socialMember.password) }
                }
            }
        }

        Given("정상적인 경우") {
            every { memberRepository.findByUserId(request.userId) } returns member
            every { passwordEncoder.matches(request.password, member.password) } returns true
            every { jwtTokenProvider.generateToken(any()) } returns tokenDto
            every { refreshTokenRepository.save(any()) } returns mockk()

            When("로그인을 하면") {
                val result = loginService.login(request, response)

                Then("로그인 성공") {
                    result.token shouldBe tokenDto.accessToken.token

                    verify { memberRepository.findByUserId(request.userId) }
                    verify { passwordEncoder.matches(request.password, member.password) }
                    verify { jwtTokenProvider.generateToken(any()) }
                    verify { refreshTokenRepository.save(any()) }
                }
            }
        }
    }
})
