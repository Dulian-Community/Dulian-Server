package dulian.dulian.domain.auth.service

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import dulian.dulian.domain.auth.entity.RefreshToken
import dulian.dulian.domain.auth.exception.RefreshTokenErrorCode
import dulian.dulian.domain.auth.repository.RefreshTokenRepository
import dulian.dulian.global.auth.jwt.components.JwtTokenProvider
import dulian.dulian.global.auth.jwt.dto.TokenDto
import dulian.dulian.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import jakarta.servlet.http.Cookie
import org.springframework.mock.web.MockHttpServletRequest

class TokenRefreshServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val refreshTokenRepository: RefreshTokenRepository = mockk()
    val jwtTokenProvider: JwtTokenProvider = mockk()

    val tokenRefreshService = TokenRefreshService(
        refreshTokenRepository,
        jwtTokenProvider
    )

    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JakartaValidationPlugin())
        .build()

    Context("토큰 갱신") {
        val request = spyk(MockHttpServletRequest())
        val refreshToken = fixtureMonkey.giveMeOne(RefreshToken::class.java)
        val token = fixtureMonkey.giveMeOne(TokenDto.Token::class.java)

        Given("쿠키에 저장된 Refresh Token이 없는 경우") {
            every { request.cookies } returns null

            When("토큰을 갱신하려하면") {
                val exception = shouldThrow<CustomException> {
                    tokenRefreshService.refresh(request)
                }

                Then("exception") {
                    exception shouldBe CustomException(RefreshTokenErrorCode.INVALID_REFRESH_TOKEN)
                }
            }
        }

        Given("DB에 저장된 Refresh Token이 없는 경우") {
            every { request.cookies } returns arrayOf(Cookie("USER_REFRESH_TOKEN", "token"))
            every {
                refreshTokenRepository.findFirstByTokenAndExpiredInAfterOrderByExpiredInDesc(any(), any())
            } returns null

            When("토큰을 갱신하려하면") {
                val exception = shouldThrow<CustomException> {
                    tokenRefreshService.refresh(request)
                }

                Then("exception") {
                    exception shouldBe CustomException(RefreshTokenErrorCode.INVALID_REFRESH_TOKEN)

                    verify {
                        refreshTokenRepository.findFirstByTokenAndExpiredInAfterOrderByExpiredInDesc(
                            any(), any()
                        )
                    }
                }
            }
        }

        Given("정상적인 경우") {
            every { request.cookies } returns arrayOf(Cookie("USER_REFRESH_TOKEN", "token"))
            every {
                refreshTokenRepository.findFirstByTokenAndExpiredInAfterOrderByExpiredInDesc(any(), any())
            } returns refreshToken
            every { jwtTokenProvider.generateAccessToken(any()) } returns token

            When("토큰을 갱신하려하면") {
                val result = tokenRefreshService.refresh(request)

                Then("exception") {
                    result shouldBe token

                    verify {
                        refreshTokenRepository.findFirstByTokenAndExpiredInAfterOrderByExpiredInDesc(
                            any(), any()
                        )
                    }
                }
            }
        }
    }
})
