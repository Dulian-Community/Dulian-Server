package dulian.dulian.domain.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.security.crypto.password.PasswordEncoder

class SignupDto {

    data class Request(
        @field:NotBlank(message = "아이디를 입력해주세요.")
        @field:Pattern(
            regexp = "^(?=.*[a-z])(?=.*\\d)[a-z\\d]{8,12}\$",
            message = "아이디는 영문 소문자와 숫자 조합으로 8~12자리로 입력해주세요."
        )
        val userId: String,

        @field:NotBlank(message = "이메일을 입력해주세요.")
        @field:Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\$", message = "이메일 형식을 확인해주세요.")
        val email: String,

        @field:NotBlank(message = "이메일 인증번호를 입력해주세요.")
        val emailConfirmCode: String,

        @field:NotBlank(message = "비밀번호를 입력해주세요.")
        @field:Pattern(
            regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[\\W_])[a-zA-Z\\d\\W_]{9,15}\$",
            message = "비밀번호는 영문 대소문자, 숫자, 특수문자 조합으로 9~15자리로 입력해주세요."
        )
        var password: String,

        @field:NotBlank(message = "비밀번호 확인을 입력해주세요.")
        val passwordConfirm: String,

        @field:NotBlank(message = "닉네임을 입력해주세요.")
        @field:Pattern(regexp = "^[a-zA-Z가-힣\\d]{3,10}\$", message = "닉네임은 한글, 영문, 숫자 조합으로 3~10자리로 입력해주세요.")
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