package dulian.dulian.domain.auth.dto

import jakarta.validation.constraints.NotBlank

class FindIdDto {

    data class Step1Request(
        @field:NotBlank(message = "이메일을 입력해주세요.")
        val email: String
    )

    data class Step2Request(
        @field:NotBlank(message = "이메일을 입력해주세요.")
        val email: String,

        @field:NotBlank(message = "인증 코드를 입력해주세요.")
        val code: String
    )

    data class Response(
        val userId: String
    )
}
