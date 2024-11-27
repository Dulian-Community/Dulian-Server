package dulian.dulian.domain.mail.entity

import dulian.dulian.domain.mail.enums.EmailTemplateCode
import dulian.dulian.global.config.db.enums.UseFlag
import dulian.dulian.global.utils.ClientUtils
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@Comment("이메일 전송 로그 정보")
@EntityListeners(AuditingEntityListener::class)
class EmailLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_log_id", nullable = false, updatable = false)
    @Comment("이메일 전송 로그 IDX")
    val emailLogId: Long? = null,

    @Column(name = "success_flag", length = 1, nullable = false, columnDefinition = "VARCHAR(1)")
    @Enumerated(EnumType.STRING)
    @Comment("이메일 전송 성공 여부(Y/N)")
    val successFlag: UseFlag,

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

    @Column(name = "email", length = 50, nullable = false, updatable = false)
    @Comment("이메일")
    val email: String,

    @Column(name = "access_ip", length = 30, nullable = false, updatable = false)
    @Comment("접속 IP")
    val accessIp: String,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("등록일자")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {

    companion object {

        /**
         * 이메일 전송 성공 로그 생성
         */
        fun ofFailLog(
            email: String,
            emailTemplateCode: EmailTemplateCode
        ) = EmailLog(
            successFlag = UseFlag.N,
            emailTemplateCode = emailTemplateCode,
            email = email,
            accessIp = ClientUtils.getClientIp()
        )

        /**
         * 이메일 전송 실패 로그 생성
         */
        fun ofSuccessLog(
            email: String,
            emailTemplateCode: EmailTemplateCode
        ) = EmailLog(
            successFlag = UseFlag.Y,
            emailTemplateCode = emailTemplateCode,
            email = email,
            accessIp = ClientUtils.getClientIp()
        )
    }
}
