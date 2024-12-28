package dulian.dulian.domain.board.repository

import com.querydsl.core.group.GroupBy.groupBy
import com.querydsl.core.group.GroupBy.list
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import dulian.dulian.domain.auth.entity.QMember.member
import dulian.dulian.domain.board.dto.BoardDto
import dulian.dulian.domain.board.entity.QBoard.board
import dulian.dulian.domain.board.entity.QTag.tag
import dulian.dulian.domain.file.entity.QAtchFile.atchFile
import dulian.dulian.domain.file.entity.QAtchFileDetail.atchFileDetail
import dulian.dulian.global.config.db.enums.UseFlag
import org.springframework.dao.IncorrectResultSizeDataAccessException

class BoardRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : BoardRepositoryCustom {

    override fun getBoard(boardId: Long): BoardDto? {
        val result = queryFactory.select(
            board
        )
            .from(board)
            .innerJoin(board.member, member)
            .leftJoin(board.atchFile, atchFile)
            .leftJoin(atchFile.atchFileDetails, atchFileDetail)
            .leftJoin(board.tags, tag)
            .where(board.boardId.eq(boardId))
            .transform(
                groupBy(board.boardId)
                    .list(
                        Projections.constructor(
                            BoardDto::class.java,
                            board.boardId,
                            board.title,
                            board.content,
                            board.member.nickname,
                            board.viewCount,
                            Expressions.constant(9999L), // TODO : 종아요 수
                            Expressions.constant(UseFlag.Y), // TODO : 좋아요 여부
                            Expressions.constant(UseFlag.N), // TODO : 북마크 여부
                            list(
                                Projections.constructor(
                                    BoardDto.AtchFileDetailsDto::class.java,
                                    atchFileDetail.atchFileDetailId,
                                    atchFileDetail.savedFileName,
                                    atchFileDetail.s3Folder
                                )
                            ),
                            list(
                                Projections.constructor(
                                    BoardDto.Tag::class.java,
                                    tag.tagId,
                                    tag.name
                                )
                            )
                        )
                    )
            )

        // 결과가 1개 이상인 경우 에러 처리
        if (result.size > 1) {
            throw IncorrectResultSizeDataAccessException(1, result.size)
        }

        val boardDto = result.firstOrNull()?.apply {
            images = if (images?.any { it.savedFileName == null } == true) {
                // 이미지가 없는 경우 null 처리
                null
            } else {
                // 중복 제거
                images?.distinctBy {
                    it.imageId
                }
            }

            // 태그가 없는 경우 null 처리
            if (tags?.any { it.tagId == null } == true) {
                tags = null
            }
        }

        return boardDto
    }

    override fun increaseViewCount(boardId: Long) {
        queryFactory.update(board)
            .set(board.viewCount, board.viewCount.add(1))
            .where(board.boardId.eq(boardId))
            .execute()
    }
}
