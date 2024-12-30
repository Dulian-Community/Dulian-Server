package dulian.dulian.domain.board.controller

import dulian.dulian.domain.board.dto.BoardDto
import dulian.dulian.domain.board.dto.BoardModifyDto
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.dto.SearchDto
import dulian.dulian.domain.board.service.BoardService
import dulian.dulian.domain.file.dto.S3FileDto
import dulian.dulian.domain.file.enums.S3Folder
import dulian.dulian.domain.file.service.FileService
import dulian.dulian.global.common.ApiResponse
import dulian.dulian.global.common.PageResponseDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/board")
class BoardController(
    private val boardService: BoardService,
    private val fileService: FileService
) {

    /**
     * 게시물 목록 조회 API
     */
    @GetMapping("/search")
    fun searchBoard(
        @ModelAttribute @Valid request: SearchDto.Request
    ): ResponseEntity<ApiResponse<PageResponseDto<SearchDto.Response>>> {
        return ApiResponse.success(boardService.search(request))
    }

    /**
     * 게시물 등록 API
     */
    @PostMapping
    fun addBoard(
        @RequestBody @Valid request: GeneralBoardAddDto.Request
    ): ResponseEntity<ApiResponse<Unit>> {
        // 태그 글자수 체크
        request.checkTags()

        boardService.addBoard(request)

        return ApiResponse.success()
    }

    /**
     * 이미지 업로드 API
     */
    @PostMapping("/upload-image")
    fun uploadImage(
        @RequestPart image: MultipartFile
    ): ResponseEntity<ApiResponse<S3FileDto>> {
        val s3FileDto = fileService.uploadAtchFile(image, S3Folder.GENERAL_BOARD)

        return ApiResponse.success(s3FileDto)
    }

    /**
     * 게시물 상세 조회 API
     */
    @GetMapping("/{boardId}")
    fun getBoard(
        @PathVariable("boardId") boardId: Long
    ): ResponseEntity<ApiResponse<BoardDto>> {
        return ApiResponse.success(boardService.getBoard(boardId))
    }

    /**
     * 게시물 수정 API
     */
    @PutMapping
    fun modifyBoard(
        @RequestBody @Valid request: BoardModifyDto.Request
    ): ResponseEntity<ApiResponse<Unit>> {
        // Request 검증
        request.checkValid()

        boardService.modifyBoard(request)

        return ApiResponse.success()
    }

    /**
     * 게시물 삭제 API
     */
    @DeleteMapping("/{boardId}")
    fun deleteBoard(
        @PathVariable("boardId") boardId: Long
    ): ResponseEntity<ApiResponse<Unit>> {
        boardService.removeBoard(boardId)

        return ApiResponse.success()
    }
}
