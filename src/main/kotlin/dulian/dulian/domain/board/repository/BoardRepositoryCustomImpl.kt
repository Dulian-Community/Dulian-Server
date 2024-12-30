package dulian.dulian.domain.board.repository

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.group.GroupBy.groupBy
import com.querydsl.core.group.GroupBy.list
import com.querydsl.core.types.Expression
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPAQueryFactory
import dulian.dulian.domain.auth.entity.QMember.member
import dulian.dulian.domain.board.dto.BoardDto
import dulian.dulian.domain.board.dto.SearchDto
import dulian.dulian.domain.board.entity.QBoard.board
import dulian.dulian.domain.board.entity.QBoardLike.boardLike
import dulian.dulian.domain.board.entity.QBoardMark.boardMark
import dulian.dulian.domain.board.entity.QTag.tag
import dulian.dulian.domain.board.enums.SearchCondition
import dulian.dulian.domain.board.enums.SearchOrder
import dulian.dulian.domain.file.entity.QAtchFile.atchFile
import dulian.dulian.domain.file.entity.QAtchFileDetail.atchFileDetail
import dulian.dulian.global.common.PageResponseDto
import dulian.dulian.global.config.db.enums.YNFlag
import dulian.dulian.global.utils.SecurityUtils
import org.springframework.dao.IncorrectResultSizeDataAccessException
import kotlin.math.ceil

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
                            board.member.memberId,
                            board.viewCount,
                            board.createdAt,
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
                            ),
                            JPAExpressions.select(boardLike.count())
                                .from(boardLike)
                                .where(boardLike.board.eq(board)),
                            createIsLikedSubQuery(),
                            createIsMarkedSubQuery()
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
            tags = if (tags?.any { it.tagId == null } == true) {
                null
            } else {
                tags?.distinctBy {
                    it.tagId
                }
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

    override fun search(request: SearchDto.Request): PageResponseDto<SearchDto.Response> {
        // 게시물 검색 결과 조회
        val boardSearchResult = getBoardSearchResult(request)

        // 태그 조회
        val boardIds = boardSearchResult.map { it.boardId }
        val tagMap = getTags(boardIds)

        // 태그 매핑
        boardSearchResult.forEach {
            it.tags = tagMap[it.boardId] ?: emptyList()
        }

        // 총 게시물 수 조회
        val totalElements = getBoardSearchCount(request)

        return PageResponseDto(
            result = boardSearchResult,
            totalElements = totalElements,
            totalPages = ceil((totalElements / 10f).toDouble()).toInt(),
            currentPage = request.page ?: 1,
        )
    }

    /**
     * 게시물 목록 조회
     */
    private fun getBoardSearchResult(
        request: SearchDto.Request
    ): MutableList<SearchDto.Response> {
        val isAuthorized = SecurityUtils.isAuthorized()

        val searchQuery = queryFactory.select(board)
            .from(board)
            .innerJoin(board.member, member)
            .leftJoin(boardLike).on(boardLike.board.boardId.eq(board.boardId))

        // 북마크 여부 조인
        if (request.isMarkedFlag == YNFlag.Y && isAuthorized) {
            searchQuery.innerJoin(boardMark)
                .on(
                    boardMark.board.boardId.eq(board.boardId)
                        .and(boardMark.member.memberId.eq(SecurityUtils.getCurrentUserId()))
                )
        } else if (isAuthorized) {
            searchQuery.leftJoin(boardMark)
                .on(
                    boardMark.board.boardId.eq(board.boardId)
                        .and(boardMark.member.memberId.eq(SecurityUtils.getCurrentUserId()))
                )
        }

        searchQuery.where(createSearchConditions(request))
            .groupBy(board.boardId, board.viewCount, board.createdAt)
            .limit(10)
            .offset(request.firstIndex)

        // 정렬 조건 추가
        val searchOrderSpecifiers = createSearchOrder(request.searchOrder)
        searchOrderSpecifiers.forEach {
            searchQuery.orderBy(it)
        }

        // 로그인 여부에 따라 북마크 여부 값 조정
        return if (isAuthorized) {
            searchQuery.transform(
                groupBy(board.boardId)
                    .list(
                        Projections.constructor(
                            SearchDto.Response::class.java,
                            board.boardId,
                            member.nickname,
                            board.title.substring(0, 200),
                            board.content,
                            board.viewCount,
                            board.createdAt,
                            boardMark.boardMarkId,
                            boardLike.count()
                        )
                    )
            )
        } else {
            searchQuery.transform(
                groupBy(board.boardId)
                    .list(
                        Projections.constructor(
                            SearchDto.Response::class.java,
                            board.boardId,
                            member.nickname,
                            board.title.substring(0, 200),
                            board.content,
                            board.viewCount,
                            board.createdAt,
                            Expressions.constant(-1L),
                            boardLike.count()
                        )
                    )
            )
        }
    }

    /**
     * 게시물 총 개수 조회
     */
    private fun getBoardSearchCount(
        request: SearchDto.Request
    ): Long {
        val query = queryFactory.select(board.count())
            .from(board)
            .innerJoin(board.member, member)

        // 북마크 여부 조인
        val isAuthorized = SecurityUtils.isAuthorized()
        if (request.isMarkedFlag == YNFlag.Y && isAuthorized) {
            query.innerJoin(boardMark)
                .on(
                    boardMark.board.boardId.eq(board.boardId)
                        .and(boardMark.member.memberId.eq(SecurityUtils.getCurrentUserId()))
                )
        }

        return query.where(createSearchConditions(request))
            .fetchOne() ?: 0
    }

    /**
     * 게시물과 연관된 태그 조회
     */
    private fun getTags(
        boardIds: List<Long>
    ): Map<Long?, List<String>> {
        return queryFactory.select(tag)
            .from(tag)
            .where(tag.board.boardId.`in`(boardIds))
            .fetch()
            .groupBy { it.board.boardId }
            .mapValues { entry -> entry.value.map { it.name } }
    }

    /**
     * 게시물 목록 조회 검색 조건 생성
     */
    private fun createSearchConditions(
        request: SearchDto.Request
    ): BooleanBuilder? {
        val builder = BooleanBuilder()

        // 시작 날짜 조건 추가
        request.formattedStartDate?.let {
            builder.and(!board.createdAt.before(it))
        }

        // 종료 날짜 조건 추가
        request.formattedEndDate?.let {
            builder.and(!board.createdAt.after(it))
        }

        // 검색어 조건 추가
        return builder
            .and(request.query?.takeIf { it.isNotBlank() }?.let {
                when (request.searchCondition) {
                    SearchCondition.TITLE -> board.title.contains(it)
                    SearchCondition.CONTENT -> board.content.contains(it)
                    SearchCondition.NICKNAME -> member.nickname.contains(it)
                    SearchCondition.ALL -> board.title.contains(it)
                        .or(board.content.contains(it))
                        .or(member.nickname.contains(it))
                }
            })
    }

    /**
     * 게시물 목록 조회 정렬 조건 생성
     */
    private fun createSearchOrder(
        searchOrder: SearchOrder
    ): List<OrderSpecifier<*>> {
        val orderSpecifiers = mutableListOf<OrderSpecifier<*>>()

        when (searchOrder) {
            SearchOrder.LATEST -> orderSpecifiers.add(board.createdAt.desc())
            SearchOrder.VIEW -> {
                orderSpecifiers.add(board.viewCount.desc())
                orderSpecifiers.add(board.createdAt.desc())
            }

            SearchOrder.POPULAR -> {
                orderSpecifiers.add(boardLike.count().desc())
                orderSpecifiers.add(board.createdAt.desc())
            }

            SearchOrder.COMMENT -> null // TODO : 댓글순 정렬
        }

        return orderSpecifiers
    }

    /**
     * 좋아요 여부 서브 쿼리 생성
     */
    private fun createIsLikedSubQuery(): Expression<Boolean>? {
        return if (SecurityUtils.isAuthorized()) {
            JPAExpressions.select(boardLike.count())
                .from(boardLike)
                .where(
                    boardLike.board.eq(board)
                        .and(boardLike.member.memberId.eq(SecurityUtils.getCurrentUserId()))
                )
                .limit(1)
                .exists()
        } else {
            Expressions.constant(false)
        }
    }

    /**
     * 북마크 여부 서브 쿼리 생성
     */
    private fun createIsMarkedSubQuery(): Expression<Boolean>? {
        return if (SecurityUtils.isAuthorized()) {
            JPAExpressions.select(boardMark.boardMarkId)
                .from(boardMark)
                .where(
                    boardMark.board.eq(board)
                        .and(boardMark.member.memberId.eq(SecurityUtils.getCurrentUserId()))
                )
                .limit(1)
                .exists()
        } else {
            Expressions.constant(false)
        }
    }
}
