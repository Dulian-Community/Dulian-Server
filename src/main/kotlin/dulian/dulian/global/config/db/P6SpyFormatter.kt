package dulian.dulian.global.config.db

import com.p6spy.engine.logging.Category
import com.p6spy.engine.spy.P6SpyOptions
import com.p6spy.engine.spy.appender.MessageFormattingStrategy
import jakarta.annotation.PostConstruct
import org.hibernate.engine.jdbc.internal.FormatStyle
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class P6SpyFormatter : MessageFormattingStrategy {

    @PostConstruct
    fun setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().logMessageFormat = this::class.java.name
    }

    override fun formatMessage(
        connectionId: Int,
        now: String?,
        elapsed: Long,
        category: String?,
        prepared: String?,
        sql: String?,
        url: String?
    ): String {
        val formattedSql = formatSql(category, sql)
        return String.format("[%s] | %d ms | %s", category, elapsed, formatSql(category, formattedSql))
    }

    private fun formatSql(
        category: String?,
        sql: String?
    ): String? {
        if (sql != null && sql.trim().isNotEmpty() && Category.STATEMENT.name == category) {
            val trimmedSQL = sql.trim().lowercase(Locale.ROOT)
            return if (trimmedSQL.startsWith("create") || trimmedSQL.startsWith("alter") || trimmedSQL.startsWith("comment")) {
                FormatStyle.DDL.formatter.format(sql)
            } else {
                FormatStyle.HIGHLIGHT.formatter.format(FormatStyle.BASIC.formatter.format(sql))
            }
        }
        return sql;
    }
}
