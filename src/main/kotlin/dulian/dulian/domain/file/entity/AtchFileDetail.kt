package dulian.dulian.domain.file.entity

import dulian.dulian.global.config.db.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.web.multipart.MultipartFile

@Entity
@Table(indexes = [Index(name = "idx_atch_file_detail_atch_file_id", columnList = "atch_file_id")])
@Comment("첨부파일 상세 정보")
class AtchFileDetail(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "atch_file_detail_id", nullable = false, updatable = false)
    @Comment("첨부파일 상세 정보 IDX")
    val atchFileDetailId: Long? = null,

    @Column(name = "original_file_name", length = 1000, nullable = false, updatable = false)
    @Comment("원본 파일명")
    val originalFileName: String,

    @Column(name = "saved_file_name", length = 1000, nullable = false, updatable = false)
    @Comment("저장 파일명")
    val savedFileName: String,

    @Column(name = "file_extension", length = 10, nullable = false, updatable = false)
    @Comment("파일 확장자")
    val fileExtension: String,

    @Column(name = "file_size", nullable = false, updatable = false)
    @Comment("파일 크기")
    val fileSize: Long,

    @Column(name = "s3_folder", length = 100, nullable = false, updatable = false)
    @Comment("S3 폴더명")
    val s3Folder: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atch_file_id", foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("첨부파일 정보 IDX")
    var atchFile: AtchFile? = null
) : BaseEntity() {

    fun changeAtchFile(atchFile: AtchFile) {
        this.atchFile = atchFile
        atchFile.atchFileDetails.add(this)
    }

    companion object {
        fun of(
            file: MultipartFile,
            savedFileName: String,
            s3Folder: String
        ) = AtchFileDetail(
            originalFileName = file.originalFilename!!,
            savedFileName = savedFileName,
            fileExtension = file.contentType!!,
            fileSize = file.size,
            s3Folder = s3Folder
        )
    }
}
