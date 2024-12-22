package dulian.dulian.domain.board.service

import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.entity.Board
import dulian.dulian.domain.board.repository.BoardRepository
import dulian.dulian.domain.file.entity.AtchFile
import dulian.dulian.domain.file.enums.S3Folder
import dulian.dulian.domain.file.service.FileService
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val memberRepository: MemberRepository,
    private val fileService: FileService
) {

    @Transactional
    fun addBoard(
        request: GeneralBoardAddDto.Request,
        images: List<MultipartFile>?
    ) {
        // 사용자 정보 조회
        val member = memberRepository.findByUserId(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // 이미지 저장
        var atchFile: AtchFile? = null
        if (images?.isNotEmpty() == true) {
            atchFile = fileService.saveImageAtchFiles(images, S3Folder.GENERAL_BOARD)
        }

        // 게시물 저장
        val board = Board.of(request, member, atchFile)
        boardRepository.save(board)
    }
}
