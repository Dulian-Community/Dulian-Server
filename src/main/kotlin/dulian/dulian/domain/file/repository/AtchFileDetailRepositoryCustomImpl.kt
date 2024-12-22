package dulian.dulian.domain.file.repository

import com.querydsl.jpa.impl.JPAQueryFactory
import dulian.dulian.domain.file.entity.QAtchFileDetail.atchFileDetail

class AtchFileDetailRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : AtchFileDetailRepositoryCustom {

    override fun updateAtchFileDetails(
        atchFileDetailIds: List<Long>,
        atchFileId: Long
    ) {
        queryFactory.update(atchFileDetail)
            .set(atchFileDetail.atchFile.atchFileId, atchFileId)
            .where(atchFileDetail.atchFileDetailId.`in`(atchFileDetailIds))
            .execute()
    }
}
