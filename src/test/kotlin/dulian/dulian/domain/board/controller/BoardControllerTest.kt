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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
})
