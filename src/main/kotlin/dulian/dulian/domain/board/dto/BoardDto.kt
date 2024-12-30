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
    val memberId: Long,
    val viewCount: Long,
    var images: List<AtchFileDetailsDto>?,
    var tags: List<Tag>?,
    val likeCount: Long,
    @JsonIgnore
    val isLikedFlag: Boolean,
    @JsonIgnore
    val isMarkedFlag: Boolean
) {
    val isMine: YNFlag
        get() = if (this.memberId == SecurityUtils.getCurrentUserId()) YNFlag.Y else YNFlag.N

    val isLiked: YNFlag
        get() = if (this.isLikedFlag) YNFlag.Y else YNFlag.N

    val isMarked: YNFlag
        get() = if (this.isMarkedFlag) YNFlag.Y else YNFlag.N

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
