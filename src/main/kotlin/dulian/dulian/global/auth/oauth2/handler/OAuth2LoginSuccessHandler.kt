package dulian.dulian.global.auth.oauth2.handler

import dulian.dulian.domain.auth.entity.RefreshToken
import dulian.dulian.domain.auth.repository.RefreshTokenRepository
import dulian.dulian.global.auth.jwt.components.JwtTokenProvider
import dulian.dulian.global.utils.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2LoginSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${oauth2.redirect-url}")
    private val redirectUrl: String,
    @Value("\${spring.profiles.active}")
    private val activeProfile: String
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authentication: Authentication?
    ) {
        val (accessToken, refreshToken) = jwtTokenProvider.generateToken(authentication!!)

        // Refresh Token DB에 저장
        refreshTokenRepository.save(
            RefreshToken.of(
                refreshToken,
                authentication.name.toLong()
            )
        )

        // 환경에 따라 분리
        if (activeProfile == "prod") {
            // Refresh Token 쿠키에 담아 전달
            val cookie = CookieUtils.createCookie(
                "USER_REFRESH_TOKEN",
                refreshToken.token,
                refreshToken.expiresInSecond
            )
            response?.addHeader("Set-Cookie", cookie.toString())
            response?.sendRedirect("$redirectUrl/oauth2-login-success?accessToken=${accessToken.token}&accessTokenExpiresIn=${accessToken.expiresIn}")
        } else {
            response?.sendRedirect("$redirectUrl/oauth2-login-success?accessToken=${accessToken.token}&accessTokenExpiresIn=${accessToken.expiresIn}&refreshToken=${refreshToken.token}&refreshTokenExpiresIn=${refreshToken.expiresIn}")
        }
    }
}
