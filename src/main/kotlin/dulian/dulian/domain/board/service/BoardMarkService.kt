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
    fun mark(
        boardId: Long
    ) {
        // 사용자 정보 조회
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // 이미 좋아요를 누른 게시물인 경우 검증
        if (boardMarkRepository.existsByBoardBoardIdAndMemberMemberId(boardId, member.memberId!!)) {
            throw CustomException(BoardErrorCode.ALREADY_MARKED)
        }

        // 게시물 정보 조회
        val board = boardRepository.findByIdOrNull(boardId)
            ?: throw CustomException(BoardErrorCode.BOARD_NOT_FOUND)

        // 북마크 정보 저장
        val bookmark = BoardMark.of(
            board = board,
            member = member
        )
        boardMarkRepository.save(bookmark)
    }

    @Transactional
    fun unmark(
        boardId: Long
    ) {
        // 사용자 정보 조회
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // 북마크 정보 조회
        val boardLike = boardMarkRepository.findByBoardBoardIdAndMemberMemberId(boardId, member.memberId!!)
            ?: throw CustomException(BoardErrorCode.BOARD_MARK_NOT_FOUND)

        // 좋아요 정보 삭제
        boardMarkRepository.delete(boardLike)
    }
}