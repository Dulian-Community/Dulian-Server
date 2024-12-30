package dulian.dulian.domain.board.dto

import dulian.dulian.domain.board.enums.SearchCondition
import dulian.dulian.domain.board.enums.SearchOrder
import dulian.dulian.global.common.DateFormat
import dulian.dulian.global.config.db.enums.YNFlag
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SearchDto {

    data class Request(
        val page: Int?,
        val query: String?,
        val condition: String?,
        val order: String?,
        @field:DateFormat
        val startDate: String?, // TODO : 날짜 형식 맞는지 검증
        @field:DateFormat
        val endDate: String?,// TODO : 날짜 형식 맞는지 검증
        val isMarked: String?
    ) {
        val searchCondition: SearchCondition // 검색 조건
            get() = condition?.takeIf { it.isNotBlank() }?.let { SearchCondition.valueOf(it.uppercase()) }
                ?: SearchCondition.ALL

        val searchOrder: SearchOrder // 정렬 조건
            get() = order?.takeIf { it.isNotBlank() }?.let { SearchOrder.valueOf(it.uppercase()) }
                ?: SearchOrder.LATEST

        val isMarkedFlag: YNFlag // 북마크 여부
            get() = isMarked?.takeIf { it.isNotBlank() }?.let { YNFlag.valueOf(it.uppercase()) }
                ?: YNFlag.N

        val formattedStartDate: LocalDateTime? // 시작 날짜
            get() {
                return startDate?.takeIf { it.isNotBlank() }
                    ?.let {
                        LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay()
                    }
            }

        val formattedEndDate: LocalDateTime? // 종료 날짜
            get() {
                return endDate?.takeIf { it.isNotBlank() }
                    ?.let {
                        LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay()
                    }
            }

        val firstIndex: Long
            get() = ((page?.toLong() ?: 1) - 1) * 10
    }

    data class Response(
        val boardId: Long,
        val nickname: String,
        val title: String,
        val content: String,
        val viewCount: Long,
        val createdAt: LocalDateTime,
        val isMarked: YNFlag,
        val likeCount: Long
    ) {
        var tags: List<String> = emptyList()
    }
}
