package dulian.dulian.domain.board.controller

import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.service.BoardMarkService
import dulian.dulian.global.exception.CustomException
import dulian.dulian.utils.makeDocument
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
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

    beforeEach { restDocumentation.beforeTest(javaClass, it.name.testName) }
    afterEach { restDocumentation.afterTest() }

    describe("게시물 북마크 API") {
        val boardId = 1L

        context("정상적인 요청인 경우") {
            every { boardMarkService.toggleMark(boardId) } just Runs

            it("게시물 좋아요 성공") {
                mockMvc.makeDocument("게시물 북마크 성공", "게시물", "게시물 북마크 API") {
                    requestLine(HttpMethod.POST, "/api/v1/board/mark/{boardId}") {
                        pathVariable(1L)
                    }
                    pathParameters {
                        parameterWithName("boardId").description("게시물 번호")
                    }
                }
            }
        }

        context("게시물 정보가 존재하지 않는 경우") {
            every { boardMarkService.toggleMark(boardId) } throws CustomException(BoardErrorCode.BOARD_NOT_FOUND)

            it("게시물 좋아요 실패") {
                mockMvc.makeDocument("게시물 북마크 실패", "게시물", "게시물 북마크 API") {
                    requestLine(HttpMethod.POST, "/api/v1/board/mark/{boardId}") {
                        pathVariable(1L)
                    }
                    assertBuilder(status().isNotFound) {
                        assert("message", BoardErrorCode.BOARD_NOT_FOUND.message)
                    }
                }
            }
        }
    }
})
