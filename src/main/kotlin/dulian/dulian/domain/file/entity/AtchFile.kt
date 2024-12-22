package dulian.dulian.domain.file.entity

import dulian.dulian.global.config.db.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Comment("첨부파일 정보")
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
