package dulian.dulian.domain.file.repository

interface AtchFileDetailRepositoryCustom {

    fun updateAtchFileDetails(atchFileDetailIds: List<Long>, atchFileId: Long)

    fun deleteAtchFileDetailByAtchFileDetailIds(atchFileDetailIds: List<Long>)
}
