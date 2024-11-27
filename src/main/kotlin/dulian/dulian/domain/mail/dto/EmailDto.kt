package dulian.dulian.domain.mail.dto

import dulian.dulian.domain.mail.enums.EmailTemplateCode

data class EmailDto(
    val recipient: String,
    val templateCode: EmailTemplateCode,
    val variables: Map<String, Any>
)