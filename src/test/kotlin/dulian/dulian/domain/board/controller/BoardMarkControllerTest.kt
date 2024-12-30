package dulian.dulian.domain.board.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.service.BoardMarkService
import dulian.dulian.global.exception.CustomException
import dulian.dulian.utils.fixtureMonkey
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.filter.CharacterEncodingFilter

@WebMvcTest(BoardMarkController::class)
@ExtendWith(RestDocumentationExtension::class)
class BoardMarkControllerTest(
    @MockkBean
    private val boardMarkService: BoardMarkService,

    @Autowired
    private val context: WebApplicationContext,
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

    val fixtureMonkey = fixtureMonkey()

    beforeEach { restDocumentation.beforeTest(javaClass, it.name.testName) }
    afterEach { restDocumentation.afterTest() }

    describe("게시물 북마크 API") {
        val boardId = 1L

        context("정상적인 요청인 경우") {
            every { boardMarkService.toggleMark(boardId) } just Runs

            it("게시물 좋아요 성공") {
                mockMvc.perform(
                    post("/api/v1/board/mark/{boardId}", boardId)
                )
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            "board-mark",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 북마크 API")
                                    .pathParameters(
                                        parameterWithName("boardId").description("게시물 번호"),
                                    )
                                    .build()
                            )
                        )
                    )
            }
        }
        
        context("게시물 정보가 존재하지 않는 경우") {
            every { boardMarkService.toggleMark(boardId) } throws CustomException(BoardErrorCode.BOARD_NOT_FOUND)

            it("게시물 좋아요 실패") {
                mockMvc.perform(
                    post("/api/v1/board/mark/{boardId}", boardId)
                )
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("message").value(BoardErrorCode.BOARD_NOT_FOUND.message))
                    .andDo(
                        document(
                            "board-mark",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 북마크 API")
                                    .pathParameters(
                                        parameterWithName("boardId").description("게시물 번호"),
                                    )
                                    .build()
                            )
                        )
                    )
            }
        }
    }
})
