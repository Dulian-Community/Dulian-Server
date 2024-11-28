package dulian.dulian.domain.mail.entity

import dulian.dulian.domain.mail.enums.EmailTemplateCode
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Comment("이메일 인증 코드 정보")
@EntityListeners(AuditingEntityListener::class)
class EmailCode(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_code_id", nullable = false, updatable = false)
    @Comment("이메일 인증 코드 IDX")
    val emailCodeId: Long? = null,

    @Column(name = "code", length = 10, nullable = false, updatable = false)
    @Comment("인증 코드")
    val code: String,

    @Column(name = "email", length = 50, nullable = false, updatable = false)
    @Comment("이메일")
    val email: String,

    @Column(
        name = "email_template_code",
        length = 30,
        nullable = false,
        updatable = false,
        columnDefinition = "VARCHAR(30)"
    )
    @Enumerated(EnumType.STRING)
    @Comment("이메일 템플릿 코드")
    val emailTemplateCode: EmailTemplateCode,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("등록일자")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    companion object {

        fun ofSignupConfirm(
            code: Int,
            email: String
        ) = EmailCode(
            code = code.toString(),
            email = email,
            emailTemplateCode = EmailTemplateCode.SIGNUP_CONFIRM
        )
    }
}
