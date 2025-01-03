package dulian.dulian.utils.rest_docs

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder
import org.springframework.http.HttpMethod
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.RequestPartBodySnippet
import org.springframework.restdocs.request.ParameterDescriptor
import org.springframework.restdocs.snippet.Snippet
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

class CustomRestDocsBuilder(
    private val mockMvc: MockMvc,
    private val identifier: String,
    private val tag: String,
    private val summary: String,
) {
    private var requestBuilder: MockHttpServletRequestBuilder? = null
    private var multipartRequestBuilder: MockMultipartHttpServletRequestBuilder? = null
    private var assertBuilder: (AssertBuilder.() -> Unit)? = null
    private var status: ResultMatcher = MockMvcResultMatchers.status().isOk

    private var pathParameterDescriptors: MutableList<ParameterDescriptor>? = null
    private var responseBodyDescriptors: MutableList<FieldDescriptor>? = null
    private var requestBodyDescriptors: MutableList<FieldDescriptor>? = null
    private var queryParameterDescriptors: MutableList<ParameterDescriptor>? = null
    private var requestPartBodySnippets: MutableList<RequestPartBodySnippet>? = null

    fun build() {
        // Create Result Actions
        val resultActions = createResultActions()

        // Request Assert
        assertBuilder?.let {
            AssertBuilder(status, resultActions).apply(it).build()
        }

        // Create Resource
        val resourceBuilder = createResourceBuilder()

        // Snippets
        val snippets = createSnippets(resourceBuilder)

        resultActions.andDo(
            document(
                identifier,
                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                *snippets.toTypedArray()
            ),
        )
    }

    /**
     * Request Builder 생성
     *
     * @param method Http Method
     * @param url 요청 URL
     * @param requestBuilder RequestBuilder를 구성하는 람다 함수
     */
    fun requestLine(
        method: HttpMethod,
        url: String,
        requestBuilder: RequestBuilder.() -> Unit
    ) {
        this.requestBuilder = RequestBuilder(method, url).apply(requestBuilder).build()
    }

    /**
     * Multipart Request Builder 생성
     *
     * @param method Http Method
     * @param url 요청 URL
     * @param requestBuilder RequestBuilder를 구성하는 람다 함수
     */
    fun multipartRequestLine(
        method: HttpMethod,
        url: String,
        requestBuilder: MultipartRequestBuilder.() -> Unit
    ) {
        this.multipartRequestBuilder = MultipartRequestBuilder(method, url).apply(requestBuilder).build()
    }

    /**
     * Assert Builder 생성
     *
     * @param status 예상하는 Http Status
     * @param assertBuilder AssertBuilder를 구성하는 람다 함수
     */
    fun assertBuilder(
        status: ResultMatcher = MockMvcResultMatchers.status().isOk,
        assertBuilder: (AssertBuilder.() -> Unit)? = null
    ) {
        this.status = status
        this.assertBuilder = assertBuilder
    }

    /**
     * PathParameterDescriptors 생성
     *
     * @param pathParameterBuilder PathParameterBuilder를 구성하는 람다 함수
     */
    fun pathParameters(
        pathParameterBuilder: (PathParameterBuilder.() -> Unit)
    ) {
        this.pathParameterDescriptors = PathParameterBuilder().apply(pathParameterBuilder).pathParameterDescriptors
    }

    /**
     * ResponseBodyDescriptors 생성
     *
     * @param responseBodyBuilder ResponseBodyBuilder를 구성하는 람다 함수
     */
    fun responseBody(
        responseBodyBuilder: ResponseBodyBuilder.() -> Unit
    ) {
        this.responseBodyDescriptors = ResponseBodyBuilder().apply(responseBodyBuilder).responseBodyDescriptors
    }

    /**
     * QueryParameterDescriptors 생성
     *
     * @param queryParameterBuilder QueryParameterBuilder를 구성하는 람다 함수
     */
    fun queryParameters(
        queryParameterBuilder: QueryParameterBuilder.() -> Unit
    ) {
        this.queryParameterDescriptors = QueryParameterBuilder().apply(queryParameterBuilder).queryParameterDescriptors
    }

    /**
     * RequestBodyDescriptors 생성
     *
     * @param requestBodyBuilder RequestBodyBuilder를 구성하는 람다 함수
     */
    fun requestBody(
        requestBodyBuilder: RequestBodyBuilder.() -> Unit
    ) {
        this.requestBodyDescriptors = RequestBodyBuilder().apply(requestBodyBuilder).requestBodyDescriptors
    }

    /**
     * RequestPartBodySnippets 생성
     *
     * @param requestPartBodyBuilder RequestPartBodyBuilder를 구성하는 람다 함수
     */
    fun requestPartBody(
        requestPartBodyBuilder: RequestPartBodyBuilder.() -> Unit
    ) {
        this.requestPartBodySnippets = RequestPartBodyBuilder().apply(requestPartBodyBuilder).requestPartBodySnippets
    }

    /**
     * Result Actions 생성
     *
     * @return ResultActions
     */
    private fun createResultActions(): ResultActions {
        return if (requestBuilder != null) {
            mockMvc.perform(requestBuilder!!)
        } else if (multipartRequestBuilder != null) {
            mockMvc.perform(multipartRequestBuilder!!)
        } else {
            throw IllegalArgumentException("RequestBuilder is null")
        }
    }

    /**
     * Rest Docs를 구성하는 Resource Builder 생성
     *
     * @return ResourceSnippetParametersBuilder
     */
    private fun createResourceBuilder(): ResourceSnippetParametersBuilder {
        val resourceBuilder = ResourceSnippetParameters.builder()
            .tag(tag)
            .summary(summary)

        // Response Body
        responseBodyDescriptors?.let {
            it.forEach { itt -> println(itt.path) }
            resourceBuilder.responseFields(it)
        }

        // Request Body
        requestBodyDescriptors?.let {
            resourceBuilder.requestFields(it)
        }

        // Query Parameter
        queryParameterDescriptors?.let {
            resourceBuilder.queryParameters(*it.toTypedArray())
        }

        // Path Parameter
        pathParameterDescriptors?.let {
            resourceBuilder.pathParameters(*it.toTypedArray())
        }

        return resourceBuilder
    }

    /**
     * Rest Docs에 사용될 Snippet을 생성
     *
     * @param resourceBuilder ResourceSnippetParametersBuilder
     * @return
     */
    private fun createSnippets(
        resourceBuilder: ResourceSnippetParametersBuilder
    ): MutableList<Snippet> {
        val snippets = mutableListOf<Snippet>()

        // RequestPart Body
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

        return snippets
    }
}