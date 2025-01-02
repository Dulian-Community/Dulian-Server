package dulian.dulian.utils

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestPartBody
import org.springframework.restdocs.payload.RequestPartBodySnippet
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

fun MockMvc.makeDocument(
    identifier: String,
    tag: String,
    summary: String,
    customRestDocsBuilder: CustomRestDocsBuilder.() -> Unit
) {
    CustomRestDocsBuilder(this, identifier, tag, summary).apply(customRestDocsBuilder).build()
}

class CustomRestDocsBuilder(
    private val mockMvc: MockMvc,
    private val identifier: String,
    private val tag: String,
    private val summary: String,
) {
    private var requestBuilder: MockHttpServletRequestBuilder? = null
    private var mockMultipartRequestBuilder: MockMultipartHttpServletRequestBuilder? = null
    private var assertBuilder: (AssertBuilder.() -> Unit)? = null
    private var pathParameterBuilder: (PathParameterBuilder.() -> Unit)? = null
    private var status: ResultMatcher = MockMvcResultMatchers.status().isOk

    //    private var responseBodyBuilder: (ResponseBodyBuilder<Any>.() -> Unit)? = null
    private var responseBodyDescriptors: MutableList<FieldDescriptor>? = null
    private var requestBodyDescriptors: MutableList<FieldDescriptor>? = null
    private var queryParameterDescriptors: MutableList<ParameterDescriptor>? = null
    private var requestPartBodySnippets: MutableList<RequestPartBodySnippet>? = null

    fun build() {
        val resultActions = if (requestBuilder != null) {
            mockMvc.perform(requestBuilder!!)
        } else if (mockMultipartRequestBuilder != null) {
            mockMvc.perform(mockMultipartRequestBuilder!!)
        } else {
            throw IllegalArgumentException("RequestBuilder is null")
        }

        assertBuilder?.let {
            AssertBuilder(status, resultActions).apply(it).build()
        }

        // Path Parameters
        val parameterDescriptors = pathParameterBuilder?.let {
            PathParameterBuilder().apply(it).pathParameterDescriptors
        }

        // Resource
        val resourceBuilder = ResourceSnippetParameters.builder()
            .tag(tag)
            .summary(summary)

        responseBodyDescriptors?.let {
            resourceBuilder.responseFields(it)
        }

        requestBodyDescriptors?.let {
            resourceBuilder.requestFields(it)
        }

        // Snippets
        val snippets = mutableListOf<Snippet>()
        queryParameterDescriptors?.let {
            resourceBuilder.queryParameters(*it.toTypedArray())
        }
        parameterDescriptors?.let {
            resourceBuilder.pathParameters(*it.toTypedArray())
        }

        // RequestPartBody
        requestPartBodySnippets?.let { requestPartBodySnippets ->
            requestPartBodySnippets.forEach {
                snippets.add(
                    it
                )
            }
        }

        snippets.add(
            resource(
                resourceBuilder.build()
            )
        )

        resultActions.andDo(
            document(
                identifier,
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                *snippets.toTypedArray()
            ),
        )
    }

    fun requestLine(
        method: HttpMethod,
        url: String,
        requestBuilder: RequestBuilder.() -> Unit
    ) {
        this.requestBuilder = RequestBuilder(method, url).apply(requestBuilder).build()
    }

    fun multipartRequestLine(
        method: HttpMethod,
        url: String,
        requestBuilder: MultipartRequestBuilder.() -> Unit
    ) {
        this.mockMultipartRequestBuilder = MultipartRequestBuilder(method, url).apply(requestBuilder).build()
    }

    fun assertBuilder(
        status: ResultMatcher = MockMvcResultMatchers.status().isOk,
        assertBuilder: (AssertBuilder.() -> Unit)? = null
    ) {
        this.status = status
        this.assertBuilder = assertBuilder
    }

    fun pathParameters(
        pathParameterBuilder: (PathParameterBuilder.() -> Unit)
    ) {
        this.pathParameterBuilder = pathParameterBuilder
    }

    fun responseBody(
        responseBodyBuilder: ResponseBodyBuilder.() -> Unit
    ) {
        val test = ResponseBodyBuilder().apply(responseBodyBuilder)
        this.responseBodyDescriptors = test.responseBodyDescriptors
    }

    fun queryParameters(
        queryParameterBuilder: QueryParameterBuilder.() -> Unit
    ) {
        val test = QueryParameterBuilder().apply(queryParameterBuilder)
        this.queryParameterDescriptors = test.queryParameters
    }

    fun requestBody(
        requestBodyBuilder: RequestBodyBuilder.() -> Unit
    ) {
        val test = RequestBodyBuilder().apply(requestBodyBuilder)
        this.requestBodyDescriptors = test.requestBodyDescriptors
    }

    fun requestPartBody(
        requestPartBodyBuilder: RequestPartBodyBuilder.() -> Unit
    ) {
        val test = RequestPartBodyBuilder().apply(requestPartBodyBuilder)
        this.requestPartBodySnippets = test.requestPartBodySnippets
    }
}

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

    fun pathVariable(vararg data: Any) {
        this.uriVariables = data
    }

    fun contentType(contentType: MediaType) {
        this.contentType = contentType
    }

    fun param(name: String, vararg values: String) {
        values.forEach {
            params.add(name, it)
        }
    }

    fun requestBody(content: String) {
        this.content = content
    }
}

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
            println(it)
            multipartRequestBuilder.file(it)
        }

        return multipartRequestBuilder
    }

    fun file(file: MockMultipartFile) {
        files.add(file)
    }
}

class AssertBuilder(
    private val status: ResultMatcher = MockMvcResultMatchers.status().isOk,
    private var resultActions: ResultActions
) {
    //    private var status: ResultMatcher = MockMvcResultMatchers.status().isOk
    private val resultMatchers: MutableList<ResultMatcher> = mutableListOf()

    fun build() {
        resultActions = resultActions.andExpect(status)
        resultMatchers.forEach {
            resultActions = resultActions.andExpect(it)
        }
    }

    fun assert(
        path: String,
        value: Any?
    ) {
        resultMatchers.add(
            jsonPath(path).value(value)
        )
    }
}

class PathParameterBuilder {
    val pathParameterDescriptors = mutableListOf<ParameterDescriptor>()

    fun field(
        name: String,
        description: String
    ) {
        pathParameterDescriptors.add(
            parameterWithName(name).description(description)
        )
    }
}

class ResponseBodyBuilder {
    val responseBodyDescriptors = mutableListOf<FieldDescriptor>()

    fun field(
        name: String,
        description: String
    ) {
        responseBodyDescriptors.add(
            fieldWithPath(name).description(description)
        )
    }
}

class QueryParameterBuilder {
    val queryParameters = mutableListOf<ParameterDescriptor>()

    fun param(
        name: String,
        description: String
    ) {
        queryParameters.add(
            parameterWithName(name).description(description)
        )
    }
}

class RequestBodyBuilder {
    val requestBodyDescriptors = mutableListOf<FieldDescriptor>()

    fun field(
        name: String,
        description: String
    ) {
        requestBodyDescriptors.add(
            fieldWithPath(name).description(description)
        )
    }
}

class RequestPartBodyBuilder {
    val requestPartBodySnippets = mutableListOf<RequestPartBodySnippet>()

    fun field(
        name: String
    ) {
        requestPartBodySnippets.add(
            requestPartBody(name)
        )
    }
}
