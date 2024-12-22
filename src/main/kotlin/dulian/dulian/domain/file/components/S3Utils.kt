package dulian.dulian.domain.file.components

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import dulian.dulian.domain.file.enums.S3Folder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class S3Utils(
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String,
    private val amazonS3Client: AmazonS3Client
) {

    fun uploadFile(
        fileName: String,
        file: MultipartFile,
        s3Folder: S3Folder
    ) {
        val metaData = ObjectMetadata()
        metaData.contentType = file.contentType
        metaData.contentLength = file.size

        val bucketName = bucket + s3Folder.folderName
        amazonS3Client.putObject(bucketName, fileName, file.inputStream, metaData)
    }
}
