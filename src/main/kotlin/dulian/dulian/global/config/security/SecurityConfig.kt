package dulian.dulian.global.config.security

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
class SecurityConfig {

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

        return http.build()
    }

    companion object {
        // 허용 URL
        private val PERMIT_ALL = arrayOf(
            "/api/v1/auth/signup",
            "/api/v1/auth/signup/send-email-confirm-code"
        )
    }
}