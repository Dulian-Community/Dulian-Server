package dulian.dulian.domain.board.repository

import dulian.dulian.domain.board.dto.BoardDto

interface BoardRepositoryCustom {

    fun getBoard(boardId: Long): BoardDto?

    fun increaseViewCount(boardId: Long)
}
