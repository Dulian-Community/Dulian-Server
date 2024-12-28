package dulian.dulian.domain.board.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import dulian.dulian.domain.board.entity.QTag.tag
import dulian.dulian.global.config.db.enums.UseFlag

class TagRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : TagRepositoryCustom {

    override fun deleteTagByTagIds(tagIds: List<Long>) {
        queryFactory.update(tag)
            .set(tag.useFlag, UseFlag.N)
            .where(tag.tagId.`in`(tagIds))
            .execute()
    }
}
