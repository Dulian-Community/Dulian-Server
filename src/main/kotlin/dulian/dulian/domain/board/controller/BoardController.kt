package dulian.dulian.domain.board.controller

import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.service.BoardService
import dulian.dulian.global.common.ApiResponse
import dulian.dulian.global.exception.CustomException
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/board")
class BoardController(
    private val boardService: BoardService
) {
    /**
     * 게시물 등록 API
     */
    @PostMapping
    fun addBoard(
        @RequestPart @Valid request: GeneralBoardAddDto.Request,
        @RequestPart(required = false) images: List<MultipartFile>?
    ): ResponseEntity<ApiResponse<Unit>> {
        // 이미지 최대 개수 체크
        checkImages(images)

        // 태그 글자수 체크
        request.checkTags()

        boardService.addBoard(request, images)

        return ApiResponse.success()
    }

    /**
     * 이미지 최대 개수 체크
     */
    private fun checkImages(
        images: List<MultipartFile>?
    ) {
        if (images != null) {
            val imageCount = images.count { !it.isEmpty }
            require(imageCount <= 9) {
                throw CustomException(BoardErrorCode.TOO_MANY_9_IMAGES)
            }
        }
    }
}
