package dulian.dulian.domain.board.service

import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.entity.BoardMark
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.repository.BoardMarkRepository
import dulian.dulian.domain.board.repository.BoardRepository
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BoardMarkService(
    private val boardMarkRepository: BoardMarkRepository,
    private val memberRepository: MemberRepository,
    private val boardRepository: BoardRepository
) {

    @Transactional
    fun toggleMark(
        boardId: Long
    ) {
        // 사용자 정보 조회
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // 게시물 정보 조회
        val board = boardRepository.findByIdOrNull(boardId)
            ?: throw CustomException(BoardErrorCode.BOARD_NOT_FOUND)

        // 북마크 저장 or 삭제
        boardMarkRepository.findByBoardBoardIdAndMemberMemberId(boardId, member.memberId!!)?.let {
            boardMarkRepository.delete(it)
        } ?: run {
            val boardMark = BoardMark.of(
                board = board,
                member = member
            )
            boardMarkRepository.save(boardMark)
        }
    }
}
