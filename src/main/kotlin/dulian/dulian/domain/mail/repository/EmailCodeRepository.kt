package dulian.dulian.domain.mail.repository

import dulian.dulian.domain.mail.entity.EmailCode
import org.springframework.data.jpa.repository.JpaRepository

interface EmailCodeRepository : JpaRepository<EmailCode, Long> {

    fun findByCodeAndEmail(code: String, email: String): EmailCode?
}
