package dulian.dulian.global.utils

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object ClientUtils {

    /**
     * 클라이언트 IP 주소 조회
     *
     * @return 클라이언트 IP 주소
     */
    fun getClientIp(): String {
        val request = getRequest()
        val ip = request.getHeader("X-Forwarded-For")

        return if (ip == null) {
            request.remoteAddr
        } else {
            return ip
        }
    }

    /**
     * HttpServletRequest 조회
     *
     * @return HttpServletRequest
     */
    private fun getRequest(): HttpServletRequest {
        val requestAttributes = RequestContextHolder.currentRequestAttributes()
        return (requestAttributes as ServletRequestAttributes).request
    }
}