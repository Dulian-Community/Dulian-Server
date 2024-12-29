package dulian.dulian.domain.board.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import dulian.dulian.domain.board.entity.QTag.tag
import dulian.dulian.global.utils.SecurityUtils
import java.time.LocalDateTime

class TagRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : TagRepositoryCustom {

    override fun deleteTagByTagIds(tagIds: List<Long>) {
        queryFactory.update(tag)
            .set(tag.deletedAt, LocalDateTime.now())
            .set(tag.deletedBy, SecurityUtils.getCurrentUserId())
            .where(tag.tagId.`in`(tagIds))
            .execute()
    }
}
