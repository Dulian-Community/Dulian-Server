package dulian.dulian.domain.auth.dto

import jakarta.validation.constraints.NotBlank

class LoginDto {

    data class Request(
        @field:NotBlank(message = "아이디를 입력해주세요.") val userId: String,
        @field:NotBlank(message = "비밀번호를 입력해주세요.") val password: String
    )
}
