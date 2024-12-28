package dulian.dulian.domain.board.repository

import dulian.dulian.domain.board.entity.Board
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface BoardRepository : JpaRepository<Board, Long>, BoardRepositoryCustom {

    @EntityGraph(attributePaths = ["tags", "atchFile", "atchFile.atchFileDetails"])
    fun findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(boardId: Long): Board?
}
