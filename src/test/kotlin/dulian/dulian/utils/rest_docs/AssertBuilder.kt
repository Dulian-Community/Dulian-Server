package dulian.dulian.utils.rest_docs

import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

class AssertBuilder(
    private val status: ResultMatcher = MockMvcResultMatchers.status().isOk,
    private var resultActions: ResultActions
) {
    private val resultMatchers: MutableList<ResultMatcher> = mutableListOf()

    fun build() {
        resultActions = resultActions.andExpect(status)
        resultMatchers.forEach {
            resultActions = resultActions.andExpect(it)
        }
    }

    /**
     * Assert the value of the path
     *
     * @param path path to assert
     * @param value expected value
     */
    fun assert(
        path: String,
        value: Any?
    ) {
        resultMatchers.add(
            jsonPath(path).value(value)
        )
    }

    /**
     * Assert the value of the path with prefix
     *
     * @param prefix prefix of the path
     * @param assertPrefixBuilder lambda function to add fields with prefix
     */
    fun prefix(
        prefix: String,
        assertPrefixBuilder: AssertPrefixBuilder.() -> Unit
    ) {
        AssertPrefixBuilder().apply(assertPrefixBuilder).resultMatchersWithPrefix.forEach { (key, value) ->
            resultMatchers.add(
                jsonPath("$prefix.$key").value(value)
            )
        }
    }

    class AssertPrefixBuilder {
        val resultMatchersWithPrefix: MutableMap<String, Any?> = mutableMapOf()

        /**
         * Assert the value of the path with prefix
         *
         * @param path path to assert
         * @param value expected value
         */
        fun assert(
            path: String,
            value: Any?
        ) {
            resultMatchersWithPrefix[path] = value
        }

        /**
         * Assert the value of the path with prefix
         *
         * @param prefix prefix of the path
         * @param assertPrefixBuilder lambda function to add fields with prefix
         */
        fun prefix(
            prefix: String,
            assertPrefixBuilder: AssertPrefixBuilder.() -> Unit
        ) {
            AssertPrefixBuilder().apply(assertPrefixBuilder).resultMatchersWithPrefix.forEach { (key, value) ->
                resultMatchersWithPrefix["$prefix.$key"] = value
            }
        }
    }
}