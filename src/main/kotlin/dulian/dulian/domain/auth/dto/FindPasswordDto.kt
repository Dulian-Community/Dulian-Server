package dulian.dulian.domain.auth.dto

import jakarta.validation.constraints.NotBlank

class FindPasswordDto {

    data class Request(
        @field:NotBlank(message = "아이디를 입력해주세요.")
        val userId: String,
        @field:NotBlank(message = "이메일을 입력해주세요.")
        val email: String
    )
}
