package dulian.dulian.domain.auth.dto

import jakarta.validation.constraints.NotBlank

class SignupConfirmDto {

    data class Request(
        @field:NotBlank(message = "이메일을 입력해주세요.")
        val email: String
    )
}