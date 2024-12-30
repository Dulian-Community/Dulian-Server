package dulian.dulian.domain.board.service

import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.entity.BoardLike
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.repository.BoardLikeRepository
import dulian.dulian.domain.board.repository.BoardRepository
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BoardLikeService(
    private val boardLikeRepository: BoardLikeRepository,
    private val memberRepository: MemberRepository,
    private val boardRepository: BoardRepository
) {

    @Transactional
    fun toggleLike(
        boardId: Long
    ) {
        // 사용자 정보 조회
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // 게시물 정보 조회
        val board = boardRepository.findByIdOrNull(boardId)
            ?: throw CustomException(BoardErrorCode.BOARD_NOT_FOUND)
        
        // 좋아요 저장 or 삭제
        boardLikeRepository.findByBoardBoardIdAndMemberMemberId(boardId, member.memberId!!)?.let {
            boardLikeRepository.delete(it)
        } ?: run {
            val boardLike = BoardLike.of(
                board = board,
                member = member
            )
            boardLikeRepository.save(boardLike)
        }
    }
}
