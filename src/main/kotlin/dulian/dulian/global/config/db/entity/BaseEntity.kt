package dulian.dulian.global.config.db.entity

import dulian.dulian.global.config.db.enums.UseFlag
import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.Comment
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class BaseEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("등록일자")
    lateinit var createdAt: LocalDateTime
        protected set

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 50)
    @Comment("등록자")
    lateinit var createdBy: String
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("수정일자")
    lateinit var updatedAt: LocalDateTime
        protected set

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 50)
    @Comment("수정자")
    lateinit var updatedBy: String
        protected set

    @Column(name = "use_flag", nullable = false, insertable = false, length = 1)
    @Enumerated(EnumType.STRING)
    @Comment("사용여부")
    @ColumnDefault("'Y'")
    lateinit var useFlag: UseFlag
        protected set
}
