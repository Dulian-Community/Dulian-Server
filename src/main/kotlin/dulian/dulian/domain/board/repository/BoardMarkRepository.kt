package dulian.dulian.domain.board.repository

import dulian.dulian.domain.board.entity.BoardMark
import org.springframework.data.jpa.repository.JpaRepository

interface BoardMarkRepository : JpaRepository<BoardMark, Long> {

    fun existsByBoardBoardIdAndMemberMemberId(boardId: Long, memberId: Long): Boolean

    fun findByBoardBoardIdAndMemberMemberId(boardId: Long, memberId: Long): BoardMark?
}