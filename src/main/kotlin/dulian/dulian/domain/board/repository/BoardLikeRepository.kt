package dulian.dulian.domain.board.repository

import dulian.dulian.domain.board.entity.BoardLike
import org.springframework.data.jpa.repository.JpaRepository

interface BoardLikeRepository : JpaRepository<BoardLike, Long> {

    fun existsByBoardBoardIdAndMemberMemberId(boardId: Long, memberId: Long): Boolean

    fun findByBoardBoardIdAndMemberMemberId(boardId: Long, memberId: Long): BoardLike?
}