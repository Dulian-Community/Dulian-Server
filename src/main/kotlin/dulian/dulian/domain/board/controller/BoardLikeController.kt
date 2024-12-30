package dulian.dulian.domain.board.controller

import dulian.dulian.domain.board.service.BoardLikeService
import dulian.dulian.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/board")
class BoardLikeController(
    private val boardLikeService: BoardLikeService
) {

    /**
     * 게시물 좋아요 API
     */
    @PostMapping("/like/{boardId}")
    fun like(
        @PathVariable("boardId") boardId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        boardLikeService.like(boardId)

        return ApiResponse.success()
    }

    /**
     * 게시물 좋아요 취소 API
     */
    @PostMapping("/unlike/{boardId}")
    fun unlike(
        @PathVariable("boardId") boardId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        boardLikeService.unlike(boardId)

        return ApiResponse.success()
    }
}