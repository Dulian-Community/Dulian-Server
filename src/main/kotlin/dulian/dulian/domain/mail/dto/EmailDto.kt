package dulian.dulian.domain.mail.dto

import dulian.dulian.domain.mail.enums.EmailTemplateCode
import dulian.dulian.global.utils.RandomUtils

data class EmailDto(
    val recipient: String,
    val templateCode: EmailTemplateCode,
    val variables: Map<String, Any>
) {

    fun getCode(): Int {
        return variables["code"] as Int? ?: throw IllegalArgumentException("code is null")
    }

    companion object {

        fun ofSignupConfirm(
            recipient: String
        ) = EmailDto(
            recipient = recipient,
            templateCode = EmailTemplateCode.SIGNUP_CONFIRM,
            variables = mapOf("code" to RandomUtils.generateRandomNumbers(6))
        )
    }
}
