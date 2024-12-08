package dulian.dulian.global.auth.jwt.filter

import dulian.dulian.global.auth.jwt.components.JwtTokenProvider
import dulian.dulian.global.exception.CustomException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        try {
            if (!token.isNullOrBlank() && jwtTokenProvider.validateToken(token)) {
                val authentication = jwtTokenProvider.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: CustomException) {
            SecurityContextHolder.clearContext()
            request.setAttribute("exception", e)
            return
        }

        filterChain.doFilter(request, response)
    }

    /**
     * Header에서 Token을 추출
     *
     * @param request HttpServletRequest
     * @return Token
     */
    private fun resolveToken(
        request: HttpServletRequest
    ): String? {
        val bearerToken = request.getHeader("Authorization") ?: return null
        return if (bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
