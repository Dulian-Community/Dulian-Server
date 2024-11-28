package dulian.dulian.domain.mail.repository

import dulian.dulian.domain.mail.entity.EmailCode
import dulian.dulian.domain.mail.enums.EmailTemplateCode
import org.springframework.data.jpa.repository.JpaRepository

interface EmailCodeRepository : JpaRepository<EmailCode, Long> {

    fun findByCodeAndEmailAndEmailTemplateCode(
        code: String,
        email: String,
        emailTemplateCode: EmailTemplateCode
    ): EmailCode?
}
