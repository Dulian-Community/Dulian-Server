package dulian.dulian.domain.board.dto

import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.global.exception.CustomException
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

class GeneralBoardAddDto {

    data class Request(
        @field:NotBlank(message = "제목을 입력해주세요.")
        @field:Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
        val title: String,

        @field:NotBlank(message = "내용을 입력해주세요.")
        @field:Size(max = 10000, message = "내용은 1000자 이내로 입력해주세요.")
        val content: String,

        @field:Size(max = 5, message = "태그는 5개 이내로 입력해주세요.")
        val tags: List<String>?,

        @field:Size(max = 9, message = "이미지는 9개 이내로 입력해주세요.")
        val images: List<Long>?
    ) {
        /**
         * 태그 글자수 체크
         */
        fun checkTags() {
            this.tags?.forEach {
                require(it.length <= 10) {
                    throw CustomException(BoardErrorCode.TOO_LONG_TAG)
                }
            }
        }
    }
}
