package dulian.dulian.domain.file.dto

data class S3FileDto(
    val atchFileDetailId: Long,
    val atchFileUrl: String
) {
    companion object {
        fun of(
            atchFileDetailId: Long,
            atchFileUrl: String
        ) = S3FileDto(
            atchFileDetailId = atchFileDetailId,
            atchFileUrl = atchFileUrl
        )
    }
}
