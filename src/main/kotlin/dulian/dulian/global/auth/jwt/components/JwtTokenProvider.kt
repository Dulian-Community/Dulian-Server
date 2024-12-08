package dulian.dulian.global.auth.jwt.components

import dulian.dulian.global.auth.jwt.dto.TokenDto
import dulian.dulian.global.auth.jwt.exception.JwtErrorCode
import dulian.dulian.global.exception.CustomException
import io.jsonwebtoken.*
import io.jsonwebtoken.Jwts.SIG
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val secretKey: String,
    @Value("\${jwt.access-token-expiration}")
    private val accessTokenExpiration: Long,
    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenExpiration: Long
) {

    /**
     * Access Token, Refresh Token 생성
     */
    fun generateToken(
        authentication: Authentication
    ): TokenDto {
        val accessToken = generateAccessToken(authentication)
        val refreshToken = generateRefreshToken()

        return TokenDto(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    /**
     * Access Token 생성
     */
    fun generateAccessToken(
        authentication: Authentication
    ): TokenDto.Token {
        val authorities = authentication.authorities.joinToString(",") {
            it.authority
        }

        val now = Date().time

        // Access Token 만료 시간 설정
        val accessTokenExpiresIn = Date(now + 1000 * accessTokenExpiration)

        // Access Token 생성
        val accessToken = Jwts.builder()
            .subject(authentication.name)
            .claim("auth", authorities)
            .expiration(accessTokenExpiresIn)
            .signWith(getSigningKey(), SIG.HS256)
            .compact()

        return TokenDto.Token.of(
            token = accessToken,
            expiresIn = accessTokenExpiresIn
        )
    }

    /**
     * Access Token을 파싱하여 Authentication 객체를 반환
     */
    fun getAuthentication(
        accessToken: String
    ): Authentication {
        // 토큰 복호화
        val claims = parseClaims(accessToken)
        if (claims["auth"] == null) {
            throw CustomException(JwtErrorCode.INVALID_ACCESS_TOKEN)
        }

        // 권한정보 획득
        val authorities = claims["auth"].toString()
            .split(",")
            .map {
                SimpleGrantedAuthority(it)
            }

        val principal = User(
            claims.subject,
            "",
            authorities
        )
        return UsernamePasswordAuthenticationToken(principal, "", authorities)
    }

    /**
     * AccessToken의 유효성 검증
     */
    fun validateToken(
        accessToken: String
    ): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(accessToken)
            true
        } catch (e: SecurityException) {
            throw CustomException(JwtErrorCode.INVALID_ACCESS_TOKEN)
        } catch (e: MalformedJwtException) {
            throw CustomException(JwtErrorCode.INVALID_ACCESS_TOKEN)
        } catch (e: UnsupportedJwtException) {
            throw CustomException(JwtErrorCode.INVALID_ACCESS_TOKEN)
        } catch (e: ExpiredJwtException) {
            throw CustomException(JwtErrorCode.EXPIRED_ACCESS_TOKEN)
        }
    }

    /**
     * Refresh Token 생성
     */
    private fun generateRefreshToken(): TokenDto.Token {
        val now = Date().time

        // Refresh Token 만료 시간 설정
        val refreshTokenExpiresIn = Date(now + 1000 * refreshTokenExpiration)

        // Refresh Token 생성
        val refreshToken = Jwts.builder()
            .expiration(refreshTokenExpiresIn)
            .signWith(getSigningKey(), SIG.HS256)
            .compact()

        return TokenDto.Token.of(
            token = refreshToken,
            expiresIn = refreshTokenExpiresIn
        )
    }

    /**
     * SecretKey를 생성하는 메소드
     */
    private fun getSigningKey(): SecretKey {
        val keyBytes = Decoders.BASE64.decode(secretKey)

        return Keys.hmacShaKeyFor(keyBytes)!!
    }

    /**
     * AccessToken을 파싱하여 Claims를 반환하는 메소드
     */
    private fun parseClaims(
        accessToken: String
    ): Claims {
        return try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(accessToken)
                .payload
        } catch (e: ExpiredJwtException) {
            throw CustomException(JwtErrorCode.EXPIRED_ACCESS_TOKEN)
        }
    }
}
