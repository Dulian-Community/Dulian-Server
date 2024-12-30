package dulian.dulian.global.utils

import org.springframework.security.core.context.SecurityContextHolder

object SecurityUtils {

    private const val ANONYMOUS_USER = "anonymousUser"

    /**
     * 현재 인증된 사용자의 ID 조회
     *
     * @return 사용자 ID
     */
    fun getCurrentUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated) {
            return (authentication.name == ANONYMOUS_USER).not().let {
                authentication.name.toLong()
            }
        }

        return 0L
    }

    /**
     * 현재 사용자가 인증되어 있는지 확인
     *
     * @return 인증 여부
     */
    fun isAuthorized(): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication != null && authentication.isAuthenticated && authentication.name != ANONYMOUS_USER
    }
}
