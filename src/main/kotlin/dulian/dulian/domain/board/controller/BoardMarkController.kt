package dulian.dulian.domain.board.controller

import dulian.dulian.domain.board.service.BoardMarkService
import dulian.dulian.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/board")
class BoardMarkController(
    private val boardMarkService: BoardMarkService
) {

    /**
     * 게시물 좋아요 API
     */
    @PostMapping("/mark/{boardId}")
    fun mark(
        @PathVariable("boardId") boardId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        boardMarkService.mark(boardId)

        return ApiResponse.success()
    }

    /**
     * 게시물 좋아요 취소 API
     */
    @PostMapping("/unmark/{boardId}")
    fun unmark(
        @PathVariable("boardId") boardId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        boardMarkService.unmark(boardId)

        return ApiResponse.success()
    }
}