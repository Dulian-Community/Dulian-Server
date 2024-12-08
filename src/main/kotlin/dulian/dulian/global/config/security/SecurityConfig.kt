package dulian.dulian.global.config.security

import dulian.dulian.global.auth.oauth2.handler.OAuth2LoginFailureHandler
import dulian.dulian.global.auth.oauth2.handler.OAuth2LoginSuccessHandler
import dulian.dulian.global.auth.oauth2.service.CustomOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuth2LoginSuccessHandler: OAuth2LoginSuccessHandler,
    private val oAuth2LoginFailureHandler: OAuth2LoginFailureHandler,
    private val oAuth2UserService: CustomOAuth2UserService
) {

    /**
     * PasswordEncoder Bean 등록
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    /**
     * Security 설정
     */
    @Bean
    fun filterChain(
        http: HttpSecurity
    ): SecurityFilterChain {
        // Basic Auth 비활성화
        http
            .httpBasic { it.disable() }

        // CSRF 비활성화
        http
            .csrf { it.disable() }

        // Form Login 비활성화
        http
            .formLogin { it.disable() }

        // Session 비활성화
        http
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        // URL 권한 설정
        http
            .authorizeHttpRequests {
                it
                    .requestMatchers(*PERMIT_ALL).permitAll()
                    .anyRequest().authenticated()
            }

        // OAuth2 설정
        http
            .oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint { it.userService(oAuth2UserService) }
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler(oAuth2LoginFailureHandler)
            }

        return http.build()
    }

    companion object {
        // 허용 URL
        private val PERMIT_ALL = arrayOf(
            "/health-check",
            "/api/v1/auth/signup",
            "/api/v1/auth/signup/send-email-confirm-code",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
        )
    }
}
