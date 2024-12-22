package dulian.dulian.domain.file.service

import dulian.dulian.domain.file.components.S3Utils
import dulian.dulian.domain.file.entity.AtchFile
import dulian.dulian.domain.file.entity.AtchFileDetail
import dulian.dulian.domain.file.enums.S3Folder
import dulian.dulian.domain.file.exception.FileErrorCode
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
    @Value("\${file.image-extensions}")
    private val imageExtensions: String
) {

    @Transactional
    fun saveImageAtchFiles(
        files: List<MultipartFile>,
        s3Folder: S3Folder
    ): AtchFile {
        // 파일 확장자 체크
        checkFileExtension(files)

        // 파일 업로드 및 메타 데이터 저장
        val atchFileDetails = ArrayList<AtchFileDetail>()
        val atchFile = AtchFile()
        files.forEach {
            val savedFileName = UUID.randomUUID().toString()
            val atchFileDetail = AtchFileDetail.of(
                file = it,
                savedFileName = savedFileName,
                s3Folder = s3Folder.folderName
            )
            atchFileDetail.changeAtchFile(atchFile)
            s3Utils.uploadFile(savedFileName, it, s3Folder)
            atchFileDetails.add(atchFileDetail)
        }
        atchFileRepository.save(atchFile)

        return atchFile
    }

    /**
     * 파일 확장자 체크
     */
    private fun checkFileExtension(files: List<MultipartFile>) {
        files.forEach {
            val fileExtension = StringUtils.getFilenameExtension(it.originalFilename)!!
            require(imageExtensions.contains(fileExtension)) {
                throw CustomException(FileErrorCode.INVALID_FILE_EXTENSION)
            }
        }
    }
}
