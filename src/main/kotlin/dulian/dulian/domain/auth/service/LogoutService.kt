package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.repository.RefreshTokenRepository
import dulian.dulian.global.utils.CookieUtils
import dulian.dulian.global.utils.SecurityUtils
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LogoutService(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    @Transactional
    fun logout(
        response: HttpServletResponse
    ) {
        // 쿠키에 담긴 Refresh Token 삭제
        CookieUtils.removeCookie("USER_REFRESH_TOKEN", response)

        // DB에서 Refresh Token 삭제
        refreshTokenRepository.deleteByUserId(SecurityUtils.getCurrentUserId())
    }
}
