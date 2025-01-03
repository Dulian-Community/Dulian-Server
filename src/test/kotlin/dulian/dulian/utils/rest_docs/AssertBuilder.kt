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
}