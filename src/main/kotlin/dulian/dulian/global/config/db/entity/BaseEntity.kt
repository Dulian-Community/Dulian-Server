package dulian.dulian.global.config.db.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
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
    @Column(name = "created_by", nullable = false, updatable = false)
    @Comment("등록자")
    var createdBy: Long = 0
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("수정일자")
    lateinit var updatedAt: LocalDateTime
        protected set

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false)
    @Comment("수정자")
    var updatedBy: Long = 0
        protected set

    @Column(name = "deleted_at", insertable = false, updatable = false, columnDefinition = "DATETIME")
    @Comment("삭제일자")
    lateinit var deletedAt: LocalDateTime
        protected set

    @Column(name = "deleted_by", insertable = false, updatable = false, length = 50)
    @Comment("삭제자")
    val deletedBy: Long? = null
}
