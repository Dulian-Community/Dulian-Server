package dulian.dulian.domain.board.repository

import dulian.dulian.domain.board.dto.BoardDto
import dulian.dulian.domain.board.dto.SearchDto
import dulian.dulian.global.common.PageResponseDto

interface BoardRepositoryCustom {

    fun getBoard(boardId: Long): BoardDto?

    fun increaseViewCount(boardId: Long)

    fun search(request: SearchDto.Request): PageResponseDto<SearchDto.Response>
}
