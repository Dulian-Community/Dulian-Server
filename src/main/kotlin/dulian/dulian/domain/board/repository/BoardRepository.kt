package dulian.dulian.domain.board.repository

import dulian.dulian.domain.board.entity.Board
import org.springframework.data.jpa.repository.JpaRepository

interface BoardRepository : JpaRepository<Board, Long> {
}
