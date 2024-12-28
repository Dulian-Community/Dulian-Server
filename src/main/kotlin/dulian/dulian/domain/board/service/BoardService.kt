package dulian.dulian.domain.board.service

import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.dto.BoardDto
import dulian.dulian.domain.board.dto.BoardModifyDto
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.entity.Board
import dulian.dulian.domain.board.entity.Tag
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.repository.BoardRepository
import dulian.dulian.domain.board.repository.TagRepository
import dulian.dulian.domain.file.entity.AtchFile
import dulian.dulian.domain.file.repository.AtchFileDetailRepository
import dulian.dulian.domain.file.repository.AtchFileRepository
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val memberRepository: MemberRepository,
    private val atchFileRepository: AtchFileRepository,
    private val atchFileDetailRepository: AtchFileDetailRepository,
    private val tagRepository: TagRepository,
    @Value("\${cloud.aws.s3.url}")
    private val s3Url: String
) {

    private val log = KotlinLogging.logger { }

    @Transactional
    fun addBoard(
        request: GeneralBoardAddDto.Request,
    ) {
        // 사용자 정보 조회
        val member = memberRepository.findByUserId(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // 이미지 저장
        val atchFile = saveImage(request.images)

        // 게시물 저장
        val board = Board.of(request, member, atchFile)
        boardRepository.save(board)

        // Tag 저장
        request.tags?.forEach {
            tagRepository.save(Tag.of(it, board))
        }
    }

    @Transactional
    fun getBoard(
        boardId: Long
    ): BoardDto {
        // 게시물 조회
        val board = boardRepository.getBoard(boardId)
            ?: throw CustomException(BoardErrorCode.BOARD_NOT_FOUND)

        // 이미지 URL 설정
        board.initImageUrls(s3Url)

        // 조회수 증가
        // TODO : 동시성 제어 및 중복 조회수 증가 해결
        boardRepository.increaseViewCount(boardId)

        return board
    }

    @Transactional
    fun modifyBoard(
        request: BoardModifyDto.Request
    ) {
        // 게시물 조회
        val board = boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(request.boardId)
            ?: throw CustomException(BoardErrorCode.BOARD_NOT_FOUND)

        // 본인의 게시물인지 검증
        val memberId = board.member.memberId!!
        val member = memberRepository.findByUserId(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)
        if (memberId != member.memberId) {
            throw CustomException(BoardErrorCode.BOARD_NOT_FOUND)
        }

        // 태그 삭제
        board.tags?.let { savedTags ->
            val requestTagIds = request.savedTagIds ?: emptyList()
            val deleteTags = savedTags.filter { it.tagId !in requestTagIds }
                .map { it.tagId!! }

            if (deleteTags.isNotEmpty()) {
                tagRepository.deleteTagByTagIds(deleteTags)
            }
        }

        // 태그 저장
        request.tags?.forEach {
            val tag = Tag.of(it, board)
            tagRepository.save(tag)
        }

        // 이미지 삭제
        board.atchFile?.atchFileDetails?.let { savedImages ->
            val requestImageIds = request.images ?: emptyList()
            val deleteImages = savedImages.filter { it.atchFileDetailId!! !in requestImageIds }
                .map { it.atchFileDetailId!! }

            if (deleteImages.isNotEmpty()) {
                atchFileDetailRepository.deleteAtchFileDetailByAtchFileDetailIds(deleteImages.distinct())
            }
        }

        // 이미지 저장
        request.images?.let { images ->
            val savedAtchFile = board.atchFile
            if (savedAtchFile == null) {
                // 연관된 AtchFile 이 없는 경우 생성
                val atchFile = saveImage(request.images)
                board.createAtchFile(atchFile!!)
            } else {
                val savedImageIds = savedAtchFile.atchFileDetails.map { it.atchFileDetailId!! }
                val newImages = images.filter { it !in savedImageIds }
                if (newImages.isNotEmpty()) { // 새로운 이미지가 있는 경우
                    // 이미지가 이미 다른 게시물에 사용되었는지 체크 및 업로드한 이미지 개수와 일치하는지 체크
                    val savedAtchFileDetails = atchFileDetailRepository.findByAtchFileDetailIdIn(images).count {
                        it.atchFile?.atchFileId == null || it.atchFile?.atchFileId == savedAtchFile.atchFileId
                    }

                    if (savedAtchFileDetails != images.size) {
                        log.error { "다른 게시물에 사용된 이미지 사용 : $images" }
                        throw CustomException(CommonErrorCode.INVALID_PARAMETER)
                    }

                    atchFileDetailRepository.updateAtchFileDetails(newImages, savedAtchFile.atchFileId!!)
                }
            }
        }

        // 게시물 수정
        board.modifyBoard(
            title = request.title,
            content = request.content
        )
        boardRepository.save(board)
    }

    private fun saveImage(
        images: List<Long>?
    ): AtchFile? {
        if (images.isNullOrEmpty()) {
            return null
        }

        // 이미지가 이미 다른 게시물에 사용되었는지 체크 및 업로드한 이미지 개수와 일치하는지 체크
        val savedAtchFileDetails = atchFileDetailRepository.findByAtchFileDetailIdIn(images).count {
            it.atchFile?.atchFileId == null
        }
        if (savedAtchFileDetails != images.size) {
            log.error { "다른 게시물에 사용된 이미지 사용 : $images" }
            throw CustomException(CommonErrorCode.INVALID_PARAMETER)
        }

        val atchFile = atchFileRepository.save(AtchFile())
        atchFileDetailRepository.updateAtchFileDetails(images, atchFile.atchFileId!!)

        return atchFile
    }
}
