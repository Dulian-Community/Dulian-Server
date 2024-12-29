package dulian.dulian.domain.file.entity

import dulian.dulian.global.config.db.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Comment("첨부파일 정보")
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update atch_file set #deleted_at = now() WHERE atch_file_id = ?")
class AtchFile(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "atch_file_id", nullable = false, updatable = false)
    @Comment("첨부파일 정보 IDX")
    val atchFileId: Long? = null,

    @OneToMany(mappedBy = "atchFile", fetch = FetchType.LAZY)
    val atchFileDetails: MutableList<AtchFileDetail> = mutableListOf()
) : BaseEntity() {
}
