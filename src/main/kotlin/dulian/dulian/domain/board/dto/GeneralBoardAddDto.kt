package dulian.dulian.domain.board.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile

class GeneralBoardAddDto {

    data class Request(
        @field:NotBlank(message = "제목을 입력해주세요.")
        @field:Size(max = 100, message = "제목은 100자 이내로 입력해주세요.")
        val title: String,

        @field:NotBlank(message = "내용을 입력해주세요.")
        @field:Size(max = 1000, message = "내용은 1000자 이내로 입력해주세요.")
        val content: String,

        @field:Size(max = 5, message = "태그는 5개 이내로 입력해주세요.")
        val tags: List<String>,

        @field:Size(max = 9, message = "이미지는 9개 이내로 업로드해주세요.")
        val images: List<MultipartFile>
    )
}
