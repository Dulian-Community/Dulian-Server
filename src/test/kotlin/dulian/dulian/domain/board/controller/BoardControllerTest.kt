package dulian.dulian.domain.board.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.service.BoardService
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

@WebMvcTest(BoardController::class)
@ExtendWith(RestDocumentationExtension::class)
class BoardControllerTest(
    @MockkBean
    private val boardService: BoardService,

    @Autowired
    private val context: WebApplicationContext,

    @Autowired
    private val objectMapper: ObjectMapper
) : DescribeSpec({
    val restDocumentation = ManualRestDocumentation()
    val mockMvc = MockMvcBuilders.webAppContextSetup(context)
        .apply<DefaultMockMvcBuilder>(
            MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(Preprocessors.prettyPrint())
                .withResponseDefaults(Preprocessors.prettyPrint())
        )
        .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
        .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
        .build()

    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JakartaValidationPlugin())
        .build()

    beforeEach { restDocumentation.beforeTest(javaClass, it.name.testName) }
    afterEach { restDocumentation.afterTest() }

    describe("게시물 등록 API") {
        val request = fixtureMonkey.giveMeBuilder(GeneralBoardAddDto.Request::class.java)
            .set("tags", listOf("1234567890"))
            .sample()
        val wrongTagRequest = fixtureMonkey.giveMeBuilder(GeneralBoardAddDto.Request::class.java)
            .set("tags", listOf("12345678901"))
            .sample()
        val file = MockMultipartFile("images", "test.png", "image/jpeg", "test".toByteArray())
        val multipartRequest =
            MockMultipartFile("request", "", "application/json", objectMapper.writeValueAsString(request).toByteArray())
        val wrongTagMultipartRequest =
            MockMultipartFile(
                "request",
                "",
                "application/json",
                objectMapper.writeValueAsString(wrongTagRequest).toByteArray()
            )

        context("이미지 개수가 9개를 초과한 경우") {

            it("실패") {
                val multipartRequestBuilder = multipart("/api/v1/board")
                    .file(multipartRequest)

                List(10) { file }.forEach {
                    multipartRequestBuilder.file(it)
                }

                mockMvc.perform(
                    multipartRequestBuilder
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("message").value(BoardErrorCode.TOO_MANY_9_IMAGES.message))
                    .andDo(
                        document(
                            "실패 - 이미지 개수가 9개를 초과한 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 등록 API")
                                    .build()
                            )
                        )
                    )
            }
        }

        context("태그 글자수가 10보다 큰 경우") {
            it("실패") {
                mockMvc.perform(
                    multipart("/api/v1/board")
                        .file(wrongTagMultipartRequest)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("message").value(BoardErrorCode.TOO_LONG_TAG.message))
                    .andDo(
                        document(
                            "실패 - 태그 글자수가 10보다 큰 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 등록 API")
                                    .build()
                            )
                        )
                    )
            }
        }

        context("정상적인 요청인 경우") {
            every { boardService.addBoard(request, listOf(file)) } just Runs

            it("성공") {
                mockMvc.perform(
                    multipart("/api/v1/board")
                        .file(multipartRequest)
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            "성공 - 정상적인 요청인 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 등록 API")
                                    .build()
                            )
                        )
                    )
            }
        }
    }
})
