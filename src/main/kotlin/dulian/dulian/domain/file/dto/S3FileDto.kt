package dulian.dulian.domain.file.dto

import org.springframework.web.multipart.MultipartFile

data class S3FileDto(
    val fileName: String,
    val multipartFile: MultipartFile
)
