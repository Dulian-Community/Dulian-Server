package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.exception.RefreshTokenErrorCode
import dulian.dulian.domain.auth.repository.RefreshTokenRepository
import dulian.dulian.global.auth.enums.Role
import dulian.dulian.global.auth.jwt.components.JwtTokenProvider
import dulian.dulian.global.auth.jwt.dto.TokenDto
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TokenRefreshService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun refresh(
        request: HttpServletRequest
    ): TokenDto.Token {
        // 쿠키에 저장된 Refresh Token 조회
        val refreshToken = CookieUtils.getCookieValue(request.cookies, "USER_REFRESH_TOKEN")
            ?: throw CustomException(RefreshTokenErrorCode.INVALID_REFRESH_TOKEN)

        // Refresh Token 조회
        val savedRefreshToken = refreshTokenRepository.findFirstByTokenAndExpiredInAfterOrderByExpiredInDesc(
            refreshToken,
            LocalDateTime.now()
        ) ?: throw CustomException(RefreshTokenErrorCode.INVALID_REFRESH_TOKEN)

        // Authentication 객체 생성
        val authentication = UsernamePasswordAuthenticationToken(
            savedRefreshToken.memberId,
            "",
            listOf(Role.ROLE_USER).map { SimpleGrantedAuthority(it.name) }
        )

        // 인증 정보를 기반으로 Access Token 생성
        return jwtTokenProvider.generateAccessToken(authentication)
    }
}
