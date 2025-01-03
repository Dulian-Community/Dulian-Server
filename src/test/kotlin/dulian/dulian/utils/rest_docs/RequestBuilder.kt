package dulian.dulian.utils.rest_docs

import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class RequestBuilder(
    private val method: HttpMethod,
    private val url: String
) {
    private var uriVariables: Array<out Any> = emptyArray()
    private var contentType: MediaType = MediaType.APPLICATION_JSON
    private val params: MultiValueMap<String, String> = LinkedMultiValueMap()
    private var content: String? = null

    fun build(): MockHttpServletRequestBuilder {
        val requestBuilder = when (method) {
            HttpMethod.GET -> MockMvcRequestBuilders.get(url, *uriVariables)
            HttpMethod.POST -> MockMvcRequestBuilders.post(url, *uriVariables)
            HttpMethod.PUT -> MockMvcRequestBuilders.put(url, *uriVariables)
            HttpMethod.DELETE -> MockMvcRequestBuilders.delete(url, *uriVariables)
            else -> throw IllegalArgumentException("Invalid method")
        }
        requestBuilder.contentType(contentType)

        if (params.isNotEmpty()) {
            requestBuilder.params(params)
        }

        content?.let {
            requestBuilder.content(it)
        }

        return requestBuilder
    }

    /**
     * Set path variables
     *
     * @param data path variables
     */
    fun pathVariable(vararg data: Any) {
        this.uriVariables = data
    }

    /**
     * Set content type
     *
     * @param contentType content type
     */
    fun contentType(contentType: MediaType) {
        this.contentType = contentType
    }

    /**
     * Set request parameters
     *
     * @param name parameter name
     * @param values parameter values
     */
    fun param(name: String, vararg values: String) {
        values.forEach {
            params.add(name, it)
        }
    }

    /**
     * Set request body
     *
     * @param content request body content
     */
    fun requestBody(content: String) {
        this.content = content
    }
}