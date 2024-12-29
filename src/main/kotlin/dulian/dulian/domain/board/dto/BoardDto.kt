package dulian.dulian.domain.board.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import dulian.dulian.global.config.db.enums.YNFlag
import dulian.dulian.global.utils.SecurityUtils

data class BoardDto(
    val boardId: Long,
    val title: String,
    val content: String,
    val nickname: String,
    @JsonIgnore
    val userId: String,
    val viewCount: Long,
    val likeCount: Long,
    val isLiked: YNFlag,
    val isBookmarked: YNFlag,
    var images: List<AtchFileDetailsDto>?,
    var tags: List<Tag>?
) {
    val isMine: YNFlag
        get() = if (this.userId == SecurityUtils.getCurrentUserId()) YNFlag.Y else YNFlag.N

    // 이미지 URL 세팅
    fun initImageUrls(s3Url: String) {
        this.images?.forEach {
            it.imageUrl = "$s3Url${it.s3Folder}/${it.savedFileName}"
        }
    }

    data class AtchFileDetailsDto(
        val imageId: Long?,
        @JsonIgnore
        val savedFileName: String?,
        @JsonIgnore
        val s3Folder: String?
    ) {
        var imageUrl: String? = null
    }

    data class Tag(
        val tagId: Long?,
        val name: String?
    )
}
