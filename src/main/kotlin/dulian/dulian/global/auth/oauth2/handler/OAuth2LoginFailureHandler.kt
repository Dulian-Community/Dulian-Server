package dulian.dulian.global.auth.oauth2.handler

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

@Component
class OAuth2LoginFailureHandler(
    @Value("\${oauth2.redirect-url}")
    private val redirectUrl: String
) : AuthenticationFailureHandler {

    private val log = KotlinLogging.logger { }

    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        log.error { "OAuth2 Login Failure : ${exception?.message}" }
        response?.status = HttpServletResponse.SC_UNAUTHORIZED
        response?.sendRedirect("$redirectUrl/oauth2-login-failure")
    }
}
