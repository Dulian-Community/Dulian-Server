package dulian.dulian.domain.board.controller

import com.epages.restdocs.apispec.ConstrainedFields
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.board.dto.BoardDto
import dulian.dulian.domain.board.dto.BoardModifyDto
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.service.BoardService
import dulian.dulian.domain.file.dto.S3FileDto
import dulian.dulian.domain.file.exception.FileErrorCode
import dulian.dulian.domain.file.service.FileService
import dulian.dulian.global.exception.CustomException
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
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
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

    @MockkBean
    private val fileService: FileService,

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
        val fields = ConstrainedFields(GeneralBoardAddDto.Request::class.java)
        val fieldDescriptors = listOf(
            fields.withPath("title").description("제목"),
            fields.withPath("content").description("내용"),
            fields.withPath("tags").description("태그"),
            fields.withPath("images").description("이미지")
        )

        context("태그 글자수가 10보다 큰 경우") {
            it("실패") {
                mockMvc.perform(
                    post("/api/v1/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongTagRequest))
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
            every { boardService.addBoard(request) } just Runs

            it("성공") {
                mockMvc.perform(
                    post("/api/v1/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
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
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )
            }
        }
    }

    describe("이미지 업로드 API") {
        val file = MockMultipartFile("image", "test.png", "image/jpeg", "test".toByteArray())
        val s3FileDto = fixtureMonkey.giveMeOne(S3FileDto::class.java)

        context("허용된 확장자가 아닌 경우") {
            every {
                fileService.uploadAtchFile(any(), any())
            } throws CustomException(FileErrorCode.INVALID_FILE_EXTENSION)

            it("실패") {
                mockMvc.perform(
                    multipart("/api/v1/board/upload-image")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("message").value(FileErrorCode.INVALID_FILE_EXTENSION.message))
                    .andDo(
                        document(
                            "실패 - 허용된 확장자가 아닌 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("이미지 업로드 API")
                                    .build()
                            )
                        )
                    )
            }
        }

        context("정상적인 요청인 경우") {
            every {
                fileService.uploadAtchFile(any(), any())
            } returns s3FileDto

            it("성공") {
                mockMvc.perform(
                    multipart("/api/v1/board/upload-image")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("data.atchFileDetailId").value(s3FileDto.atchFileDetailId))
                    .andExpect(jsonPath("data.atchFileUrl").value(s3FileDto.atchFileUrl))

                    .andDo(
                        document(
                            "성공",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("이미지 업로드 API")
                                    .responseFields(
                                        fieldWithPath("data.atchFileDetailId").description("첨부파일 상세 ID"),
                                        fieldWithPath("data.atchFileUrl").description("첨부파일 URL"),
                                        fieldWithPath("status").description("상태"),
                                        fieldWithPath("statusCode").description("상태 코드"),
                                        fieldWithPath("timestamp").description("응답 시간"),
                                    )
                                    .build()
                            )
                        )
                    )
            }
        }
    }

    describe("게시물 상세 조회 API") {
        val board = fixtureMonkey.giveMeBuilder(BoardDto::class.java)
            .set("boardId", 1L)
            .set("images", listOf(BoardDto.AtchFileDetailsDto(1L, "test.png", "test")))
            .set("tags", listOf(BoardDto.Tag(1L, "tag")))
            .sample()
        val fields = ConstrainedFields(BoardDto::class.java)

        context("정상적인 요청인 경우") {
            every { boardService.getBoard(any()) } returns board

            it("게시물 상세 조회 결과 반환") {
                mockMvc.perform(
                    get("/api/v1/board/{boardId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk)
                    .andExpect(jsonPath("data.boardId").value(board.boardId))
                    .andExpect(jsonPath("data.title").value(board.title))
                    .andExpect(jsonPath("data.content").value(board.content))
                    .andExpect(jsonPath("data.nickname").value(board.nickname))
                    .andExpect(jsonPath("data.viewCount").value(board.viewCount))
                    .andExpect(jsonPath("data.likeCount").value(board.likeCount))
                    .andExpect(jsonPath("data.isLiked").value(board.isLiked.toString()))
                    .andExpect(jsonPath("data.isBookmarked").value(board.isBookmarked.toString()))
                    .andExpect(jsonPath("data.isMine").value(board.isMine.toString()))
                    .andExpect(jsonPath("data.images[0].imageId").value(board.images?.get(0)?.imageId))
                    .andExpect(jsonPath("data.images[0].imageUrl").value(board.images?.get(0)?.imageUrl))
                    .andExpect(jsonPath("data.tags[0].tagId").value(board.tags?.get(0)?.tagId))
                    .andExpect(jsonPath("data.tags[0].name").value(board.tags?.get(0)?.name))
                    .andDo(
                        document(
                            "성공",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            pathParameters(
                                parameterWithName("boardId").description("게시물 ID")
                            ),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 상세 조회 API")
                                    .responseFields(
                                        fields.withPath("data.boardId").description("게시물 ID"),
                                        fields.withPath("data.title").description("제목"),
                                        fields.withPath("data.content").description("내용"),
                                        fields.withPath("data.nickname").description("닉네임"),
                                        fields.withPath("data.viewCount").description("조회수"),
                                        fields.withPath("data.likeCount").description("좋아요 수"),
                                        fields.withPath("data.isLiked").description("좋아요 여부"),
                                        fields.withPath("data.isBookmarked").description("북마크 여부"),
                                        fields.withPath("data.isMine").description("본인 게시물 여부"),
                                        fields.withPath("data.images[0].imageId").description("이미지 ID"),
                                        fields.withPath("data.images[0].imageUrl").description("이미지 URL"),
                                        fields.withPath("data.tags[0].tagId").description("태그 ID"),
                                        fields.withPath("data.tags[0].name").description("태그 이름"),
                                        fields.withPath("status").description("상태"),
                                        fields.withPath("statusCode").description("상태 코드"),
                                        fields.withPath("timestamp").description("응답 시간"),
                                    )
                                    .build()
                            )
                        )
                    )
            }
        }

        context("게시물이 존재하지 않는 경우") {
            every { boardService.getBoard(any()) } throws CustomException(BoardErrorCode.BOARD_NOT_FOUND)

            it("에러 코드 반환") {
                mockMvc.perform(
                    get("/api/v1/board/{boardId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("message").value(BoardErrorCode.BOARD_NOT_FOUND.message))
                    .andDo(
                        document(
                            "실패 - 게시물이 존재하지 않는 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            pathParameters(
                                parameterWithName("boardId").description("게시물 ID")
                            ),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 상세 조회 API")
                                    .build()
                            )
                        )
                    )
            }
        }
    }

    describe("게시물 수정 API") {
        val request = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
            .set("tags", listOf("1234567890"))
            .sample()
        val fields = ConstrainedFields(BoardModifyDto.Request::class.java)
        val fieldDescriptors = listOf(
            fields.withPath("boardId").description("게시물 ID"),
            fields.withPath("title").description("제목"),
            fields.withPath("content").description("내용"),
            fields.withPath("tags").description("태그"),
            fields.withPath("savedTagIds").description("저장된 태그 ID"),
            fields.withPath("images").description("이미지")
        )

        context("태그 개수가 5개 초과인 경우") {
            val wrongRequest = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
                .set("tags", listOf("1", "2", "3", "4", "5", "6"))
                .sample()

            it("실패") {
                mockMvc.perform(
                    put("/api/v1/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest))
                )
                    .andExpect(status().isBadRequest)
                    .andExpect(jsonPath("message").value(BoardErrorCode.TOO_MANY_5_TAGS.message))
                    .andDo(
                        document(
                            "실패 - 태그 개수가 5개 초과인 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 수정 API")
                                    .build()
                            )
                        )
                    )
            }
        }

        context("태그 글자수가 10보다 큰 경우") {
            val wrongRequest = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
                .set("tags", listOf("12345678901"))
                .sample()

            it("실패") {
                mockMvc.perform(
                    put("/api/v1/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest))
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
                                    .summary("게시물 수정 API")
                                    .build()
                            )
                        )
                    )
            }
        }

        context("정상적인 요청인 경우") {
            every { boardService.modifyBoard(request) } just Runs

            it("성공") {
                mockMvc.perform(
                    put("/api/v1/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
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
                                    .summary("게시물 수정 API")
                                    .requestFields(fieldDescriptors)
                                    .build()
                            )
                        )
                    )
            }
        }

        context("게시물이 존재하지 않거나 본인의 게시물이 아닌 경우") {
            every { boardService.modifyBoard(any()) } throws CustomException(BoardErrorCode.BOARD_NOT_FOUND)

            it("에러 코드 반환") {
                mockMvc.perform(
                    put("/api/v1/board")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("message").value(BoardErrorCode.BOARD_NOT_FOUND.message))
                    .andDo(
                        document(
                            "실패 - 게시물이 존재하지 않거나 본인의 게시물이 아닌 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 수정 API")
                                    .build()
                            )
                        )
                    )
            }
        }
    }

    describe("게시물 삭제 API") {
        val boardId = 1L
        context("정상적인 요청인 경우") {
            every { boardService.removeBoard(any()) } just Runs

            it("성공") {
                mockMvc.perform(
                    delete("/api/v1/board/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            "성공 - 정상적인 요청인 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            pathParameters(
                                parameterWithName("boardId").description("게시물 ID")
                            ),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 삭제 API")
                                    .build()
                            )
                        )
                    )
            }
        }

        context("게시물이 존재하지 않거나 본인의 게시물이 아닌 경우") {
            every { boardService.removeBoard(any()) } throws CustomException(BoardErrorCode.BOARD_NOT_FOUND)

            it("에러 코드 반환") {
                mockMvc.perform(
                    delete("/api/v1/board/{boardId}", boardId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                    .andExpect(status().isNotFound)
                    .andExpect(jsonPath("message").value(BoardErrorCode.BOARD_NOT_FOUND.message))
                    .andDo(
                        document(
                            "실패 - 게시물이 존재하지 않거나 본인의 게시물이 아닌 경우",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                            pathParameters(
                                parameterWithName("boardId").description("게시물 ID")
                            ),
                            resource(
                                ResourceSnippetParameters.builder()
                                    .tag("게시물")
                                    .summary("게시물 삭제 API")
                                    .build()
                            )
                        )
                    )
            }
        }
    }
})
