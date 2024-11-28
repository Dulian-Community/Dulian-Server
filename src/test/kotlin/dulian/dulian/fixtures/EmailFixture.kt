package dulian.dulian.fixtures

import dulian.dulian.domain.mail.entity.EmailCode
import dulian.dulian.domain.mail.enums.EmailTemplateCode
import java.time.LocalDateTime

fun emailFixture(
    minusMinutes: Long = 2
) = EmailCode(
    code = "1234",
    email = "test@test.com",
    emailTemplateCode = EmailTemplateCode.SIGNUP_CONFIRM,
    createdAt = LocalDateTime.now().minusMinutes(minusMinutes)
)