package dulian.dulian.global.auth.jwt.handler

import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerExceptionResolver

@Component
class JwtAuthenticationEntryPoint(
    @Qualifier("handlerExceptionResolver")
    private val resolver: HandlerExceptionResolver
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authException: AuthenticationException?
    ) {
        var customException = request?.getAttribute("exception") as? CustomException
        if (customException == null) {
            customException = CustomException(CommonErrorCode.UNAUTHORIZED)
        }

        resolver.resolveException(request!!, response!!, null, customException)
    }
}
