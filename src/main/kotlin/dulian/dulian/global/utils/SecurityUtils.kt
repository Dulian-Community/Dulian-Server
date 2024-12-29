package dulian.dulian.global.utils

import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {

    private const val ANONYMOUS_USER = "anonymousUser"

    /**
     * 현재 인증된 사용자의 ID 조회
     *
     * @return 사용자 ID
     */
    fun getCurrentUserId(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated) {
            return (authentication.name == ANONYMOUS_USER).not().let {
                authentication.name
            }
        }

        return "SYSTEM"
    }
}
