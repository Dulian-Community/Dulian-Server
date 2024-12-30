package dulian.dulian.global.common

data class PageResponseDto<T>(
    val result: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int = 10
)
