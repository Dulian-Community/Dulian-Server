package dulian.dulian.global.utils

import jakarta.servlet.http.Cookie

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
    ): Cookie {
        return Cookie(cookieName, value).apply {
            this.maxAge = maxAge
            this.isHttpOnly = true
            this.secure = true
            this.path = "/"
        }
    }
}
