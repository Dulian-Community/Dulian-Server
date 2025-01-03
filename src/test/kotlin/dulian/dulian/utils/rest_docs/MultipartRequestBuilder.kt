package dulian.dulian.utils.rest_docs

import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders

class MultipartRequestBuilder(
    private val method: HttpMethod,
    private val url: String
) {
    private var uriVariables: Array<out Any> = emptyArray()
    private var contentType: MediaType = MediaType.MULTIPART_FORM_DATA
    private val files: MutableList<MockMultipartFile> = mutableListOf()

    fun build(): MockMultipartHttpServletRequestBuilder {
        val multipartRequestBuilder = MockMvcRequestBuilders.multipart(method, url, *uriVariables)
        multipartRequestBuilder.contentType(contentType)

        files.forEach {
            multipartRequestBuilder.file(it)
        }

        return multipartRequestBuilder
    }

    /**
     * Add file to the request
     *
     * @param file file to be added
     */
    fun file(file: MockMultipartFile) {
        files.add(file)
    }
}