package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.dto.LoginDto
import dulian.dulian.domain.auth.entity.RefreshToken
import dulian.dulian.domain.auth.exception.LoginErrorCode
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.auth.repository.RefreshTokenRepository
import dulian.dulian.global.auth.enums.Role
import dulian.dulian.global.auth.jwt.components.JwtTokenProvider
import dulian.dulian.global.auth.jwt.dto.TokenDto
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.CookieUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository
) {
    private val log = KotlinLogging.logger { }

    @Transactional
    fun login(
        request: LoginDto.Request,
        response: HttpServletResponse
    ): TokenDto.Token {
        // ID로 회원 조회
        val savedMember = memberRepository.findByUserId(request.userId)
            ?: throw CustomException(LoginErrorCode.FAILED_TO_LOGIN)

        // Password 일치 여부 확인
        require(passwordEncoder.matches(request.password, savedMember.password)) {
            throw CustomException(LoginErrorCode.FAILED_TO_LOGIN)
        }

        // 소셜 로그인 여부 확인
        require(savedMember.socialType == null) {
            log.error { "소셜 로그인 회원 로그인 시도 : ${savedMember.userId}" }
            throw CustomException(LoginErrorCode.FAILED_TO_LOGIN)
        }

        // Authentication 객체 생성
        val authentication = UsernamePasswordAuthenticationToken(
            savedMember.memberId,
            savedMember.password,
            listOf(Role.ROLE_USER).map { SimpleGrantedAuthority(it.name) }
        )

        // 인증 정보를 기반으로 JWT Token 생성
        val (accessToken, refreshToken) = jwtTokenProvider.generateToken(authentication)

        // Refresh Token DB에 저장
        refreshTokenRepository.save(
            RefreshToken.of(
                token = refreshToken,
                memberId = savedMember.memberId!!
            )
        )

        // Refresh Token 쿠키에 담아 전달
        val cookie = CookieUtils.createCookie(
            cookieName = "USER_REFRESH_TOKEN",
            value = refreshToken.token,
            maxAge = refreshToken.expiresInSecond
        )
        response.addHeader("Set-Cookie", cookie.toString())

        return accessToken
    }
}
