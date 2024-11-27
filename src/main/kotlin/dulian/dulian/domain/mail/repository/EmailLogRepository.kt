package dulian.dulian.domain.mail.repository

import dulian.dulian.domain.mail.entity.EmailLog
import org.springframework.data.jpa.repository.JpaRepository

interface EmailLogRepository : JpaRepository<EmailLog, Long> {
}