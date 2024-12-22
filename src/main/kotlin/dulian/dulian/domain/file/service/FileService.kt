package dulian.dulian.domain.file.service

import dulian.dulian.domain.file.components.S3Utils
import dulian.dulian.domain.file.dto.S3FileDto
import dulian.dulian.domain.file.entity.AtchFileDetail
import dulian.dulian.domain.file.enums.S3Folder
import dulian.dulian.domain.file.exception.FileErrorCode
import dulian.dulian.domain.file.repository.AtchFileDetailRepository
import dulian.dulian.domain.file.repository.AtchFileRepository
import dulian.dulian.global.exception.CustomException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class FileService(
    private val s3Utils: S3Utils,
    private val atchFileRepository: AtchFileRepository,
    private val atchFileDetailRepository: AtchFileDetailRepository,
    @Value("\${file.image-extensions}")
    private val imageExtensions: String,
    @Value("\${cloud.aws.s3.url}")
    private val s3Url: String
) {

    @Transactional
    fun uploadAtchFile(
        file: MultipartFile,
        s3Folder: S3Folder
    ): S3FileDto {
        // 파일 확장자 체크
        checkFileExtension(file)

        // 파일 업로드
        val savedFileName = UUID.randomUUID().toString()
        val atchFileDetail = AtchFileDetail.of(
            file = file,
            savedFileName = savedFileName,
            s3Folder = s3Folder.folderName
        )
        s3Utils.uploadFile(savedFileName, file, s3Folder)

        // 메타 데이터 저장
        val savedAtchFileDetail = atchFileDetailRepository.save(atchFileDetail)
        return S3FileDto.of(
            atchFileDetailId = savedAtchFileDetail.atchFileDetailId!!,
            atchFileUrl = "$s3Url${s3Folder.folderName}/$savedFileName"
        )
    }

    /**
     * 파일 확장자 체크
     */
    private fun checkFileExtension(file: MultipartFile) {
        val fileExtension = StringUtils.getFilenameExtension(file.originalFilename)!!
        require(imageExtensions.contains(fileExtension)) {
            throw CustomException(FileErrorCode.INVALID_FILE_EXTENSION)
        }
    }
}
