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
    fun like(
        boardId: Long
    ) {
        // 사용자 정보 조회
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // 이미 좋아요를 누른 게시물인 경우 검증
        if (boardLikeRepository.existsByBoardBoardIdAndMemberMemberId(boardId, member.memberId!!)) {
            throw CustomException(BoardErrorCode.ALREADY_LIKED)
        }

        // 게시물 정보 조회
        val board = boardRepository.findByIdOrNull(boardId)
            ?: throw CustomException(BoardErrorCode.BOARD_NOT_FOUND)

        // 좋아요 정보 저장
        val boardLike = BoardLike.of(
            board = board,
            member = member
        )
        boardLikeRepository.save(boardLike)
    }

    @Transactional
    fun unlike(
        boardId: Long
    ) {
        // 사용자 정보 조회
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // 좋아요 정보 조회
        val boardLike = boardLikeRepository.findByBoardBoardIdAndMemberMemberId(boardId, member.memberId!!)
            ?: throw CustomException(BoardErrorCode.BOARD_LIKE_NOT_FOUND)

        // 좋아요 정보 삭제
        boardLikeRepository.delete(boardLike)
    }
}