package dulian.dulian.domain.board.dto

import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.global.exception.CustomException
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class BoardModifyDto {

    data class Request(
        @field:Min(value = 1, message = "게시물 ID를 입력해주세요.")
        val boardId: Long,

        @field:NotBlank(message = "제목을 입력해주세요.")
        val title: String,

        @field:NotBlank(message = "내용을 입력해주세요.")
        val content: String,

        val tags: List<String>?,

        val savedTagIds: List<Long>?,

        @field:Size(max = 9, message = "이미지는 9개 이내로 입력해주세요.")
        val images: List<Long>?
    ) {

        /**
         * Request 검증
         *
         */
        fun checkValid() {
            // 태그 개수 검증(최대 5개)
            val tagsCount = this.tags?.count() ?: 0
            val savedTagIds = this.savedTagIds?.count() ?: 0
            if (tagsCount + savedTagIds > 5) {
                throw CustomException(BoardErrorCode.TOO_MANY_5_TAGS)
            }

            // 태그 글자수 검증
            this.tags?.forEach {
                require(it.length <= 10) {
                    throw CustomException(BoardErrorCode.TOO_LONG_TAG)
                }
            }
        }
    }
}
