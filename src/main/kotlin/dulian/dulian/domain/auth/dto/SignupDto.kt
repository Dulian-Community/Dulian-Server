package dulian.dulian.domain.auth.dto

import jakarta.validation.constraints.NotBlank
import org.springframework.security.crypto.password.PasswordEncoder

class SignupDto {

    data class Request(
        @field:NotBlank(message = "아이디를 입력해주세요.")
        val userId: String,

        @field:NotBlank(message = "이메일을 입력해주세요.")
        val email: String,

        @field:NotBlank(message = "이메일 인증번호를 입력해주세요.")
        val emailConfirmCode: String,

        @field:NotBlank(message = "비밀번호를 입력해주세요.")
        var password: String,

        @field:NotBlank(message = "비밀번호 확인을 입력해주세요.")
        val passwordConfirm: String,

        @field:NotBlank(message = "닉네임을 입력해주세요.")
        val nickname: String
    ) {

        // 비밀번호 일치 여부 확인
        fun checkPassword(): Boolean = password == passwordConfirm

        // 비밀번호 암호화
        fun encryptPassword(passwordEncoder: PasswordEncoder) {
            password = passwordEncoder.encode(password)
        }
    }
}