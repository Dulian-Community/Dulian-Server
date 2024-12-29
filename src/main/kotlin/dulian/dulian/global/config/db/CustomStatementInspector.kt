package dulian.dulian.global.config.db

import dulian.dulian.global.utils.SecurityUtils
import org.hibernate.resource.jdbc.spi.StatementInspector

class CustomStatementInspector : StatementInspector {

    override fun inspect(sql: String?): String {
        if (sql!!.contains(DELETE_QUERY)) {
            return sql.replace(
                DELETE_QUERY,
                DELETE_AFTER_QUERY + "'${SecurityUtils.getCurrentUserId()}'"
            )
        }
        return sql
    }

    companion object {
        private const val DELETE_QUERY = "set #deleted_at = now()"
        private const val DELETE_AFTER_QUERY = "set deleted_at = now(), deleted_by = "
    }
}
