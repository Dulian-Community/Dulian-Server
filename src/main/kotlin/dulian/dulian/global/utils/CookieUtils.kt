package dulian.dulian.global.utils

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie

object CookieUtils {

    /**
     * 쿠키 생성
     *
     * @param cookieName 쿠키명
     * @param value 쿠키값
     * @param maxAge 쿠키 만료 시간
     * @return 생성된 쿠키
     */
    fun createCookie(
        cookieName: String,
        value: String,
        maxAge: Int
    ): ResponseCookie {
        return ResponseCookie.from(cookieName, value) // TODO : 운영/개발 환경 확인 필요
            .maxAge(maxAge.toLong())
            .httpOnly(true)
            .path("/")
            .build()
    }

    /**
     * 쿠키 값 조회
     *
     * @param cookies 쿠키 배열
     * @param name 쿠키명
     * @return 쿠키값
     */
    fun getCookieValue(
        cookies: Array<Cookie>?,
        name: String
    ): String? {
        return cookies?.find { it.name == name }?.value
    }

    /**
     * 쿠키 삭제
     */
    fun removeCookie(
        cookieName: String,
        response: HttpServletResponse
    ) {
        val cookie = ResponseCookie.from(cookieName, "")
            .maxAge(0)
            .httpOnly(true)
            .path("/")
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }
}
