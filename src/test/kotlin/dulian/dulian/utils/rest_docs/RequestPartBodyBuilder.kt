package dulian.dulian.utils.rest_docs

import org.springframework.restdocs.payload.PayloadDocumentation.requestPartBody
import org.springframework.restdocs.payload.RequestPartBodySnippet


class RequestPartBodyBuilder {
    val requestPartBodySnippets = mutableListOf<RequestPartBodySnippet>()

    /**
     * Add field to the request part body
     *
     * @param name field name
     */
    fun field(
        name: String
    ) {
        requestPartBodySnippets.add(
            requestPartBody(name)
        )
    }
}
