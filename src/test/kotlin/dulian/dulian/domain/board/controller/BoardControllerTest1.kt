//package dulian.dulian.domain.board.controller
//
//import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
//import com.epages.restdocs.apispec.ResourceDocumentation.resource
//import com.epages.restdocs.apispec.ResourceSnippetParameters
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.navercorp.fixturemonkey.kotlin.giveMeOne
//import com.ninjasquad.springmockk.MockkBean
//import dulian.dulian.domain.board.dto.BoardDto
//import dulian.dulian.domain.board.dto.BoardModifyDto
//import dulian.dulian.domain.board.dto.SearchDto
//import dulian.dulian.domain.board.service.BoardService
//import dulian.dulian.domain.file.dto.S3FileDto
//import dulian.dulian.domain.file.service.FileService
//import dulian.dulian.global.common.PageResponseDto
//import dulian.dulian.utils.fixtureMonkey
//import dulian.dulian.utils.makeDocument
//import io.kotest.core.spec.style.DescribeSpec
//import io.mockk.Runs
//import io.mockk.every
//import io.mockk.just
//import org.junit.jupiter.api.extension.ExtendWith
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.http.HttpMethod
//import org.springframework.http.MediaType
//import org.springframework.mock.web.MockMultipartFile
//import org.springframework.restdocs.ManualRestDocumentation
//import org.springframework.restdocs.RestDocumentationExtension
//import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
//import org.springframework.restdocs.operation.preprocess.Preprocessors
//import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
//import org.springframework.restdocs.payload.PayloadDocumentation.requestPartBody
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
//import org.springframework.test.web.servlet.result.MockMvcResultHandlers
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
//import org.springframework.test.web.servlet.setup.MockMvcBuilders
//import org.springframework.web.context.WebApplicationContext
//import org.springframework.web.filter.CharacterEncodingFilter
//import org.springframework.web.servlet.function.RequestPredicates.contentType
//
//@WebMvcTest(BoardController::class)
//@ExtendWith(RestDocumentationExtension::class)
//class BoardControllerTest1(
//    @MockkBean
//    private val boardService: BoardService,
//
//    @MockkBean
//    private val fileService: FileService,
//
//    @Autowired
//    private val context: WebApplicationContext,
//
//    @Autowired
//    private val objectMapper: ObjectMapper
//) : DescribeSpec({
//    val restDocumentation = ManualRestDocumentation()
//    val mockMvc = MockMvcBuilders.webAppContextSetup(context)
//        .apply<DefaultMockMvcBuilder>(
//            MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
//                .operationPreprocessors()
//                .withRequestDefaults(Preprocessors.prettyPrint())
//                .withResponseDefaults(Preprocessors.prettyPrint())
//        )
//        .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
//        .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
//        .build()
//
//    val fixtureMonkey = fixtureMonkey()
//
//    beforeEach { restDocumentation.beforeTest(javaClass, it.name.testName) }
//    afterEach { restDocumentation.afterTest() }
//
//    describe("게시물 상세 조회 API") {
//        val board = fixtureMonkey.giveMeBuilder(BoardDto::class.java)
//            .set("boardId", 1L)
//            .set("images", listOf(BoardDto.AtchFileDetailsDto(1L, "test.png", "test")))
//            .set("tags", listOf(BoardDto.Tag(1L, "tag")))
//            .sample()
//
//        context("정상적인 요청인 경우") {
//            every { boardService.getBoard(any()) } returns board
//
//            it("게시물 상세 조회 결과 반환") {
//                mockMvc.makeDocument("성공!!!!", "게시물", "게시물 상세 조회 API!!") {
//                    requestLine(HttpMethod.GET, "/api/v1/board/{boardId}") {
//                        pathVariable(1L)
//                    }
//                    pathParameters {
//                        field("boardId", "게시물 ID!!!!!111")
//                    }
//                    responseBody {
//                        field("data.boardId", "게시물 ID!!!")
//                        field("data.title", "제목!!")
//                        field("data.content", "내용")
//                        field("data.nickname", "닉네임")
//                        field("data.viewCount", "조회수")
//                        field("data.likeCount", "좋아요 수")
//                        field("data.isLiked", "좋아요 여부")
//                        field("data.isMarked", "북마크 여부")
//                        field("data.isMine", "본인 게시물 여부")
//                        field("data.createdAt", "작성일")
//                        field("data.images[0].imageId", "이미지 ID")
//                        field("data.images[0].imageUrl", "이미지 URL")
//                        field("data.tags[0].tagId", "태그 ID")
//                        field("data.tags[0].name", "태그 이름")
//                        field("status", "상태")
//                        field("statusCode", "상태 코드")
//                        field("timestamp", "응답 시간")
//                    }
//                }
//            }
//        }
//    }
//
//    describe("게시물 목록 조회 API") {
//        val request = SearchDto.Request(
//            page = 1,
//            query = "검색어",
//            condition = "ALL",
//            order = "VIEW",
//            startDate = "2025-01-01",
//            endDate = "2025-12-31",
//            isMarked = "Y"
//        )
//        val response = fixtureMonkey.giveMeOne<SearchDto.Response>()
//        response.tags = listOf("tag1", "tag2")
//
//        val pageResponse = PageResponseDto(
//            result = listOf(response),
//            totalElements = 1,
//            totalPages = 1,
//            currentPage = 1,
//            pageSize = 10
//        )
//        context("정상적인 요청인 경우") {
//            every { boardService.search(request) } returns pageResponse
//
//            it("게시물 목록 반환") {
//                mockMvc.makeDocument("게시물 목록 조회 - 성공", "게시물", "게시물 목록 조회") {
//                    requestLine(HttpMethod.GET, "/api/v1/board/search") {
//                        param("page", "1")
//                        param("query", "검색어")
//                        param("condition", "ALL")
//                        param("order", "VIEW")
//                        param("startDate", "2025-01-01")
//                        param("endDate", "2025-12-31")
//                        param("isMarked", "Y")
//                    }
//                    queryParameters {
//                        param("page", "페이지 번호")
//                        param("query", "검색어")
//                        param("condition", "검색 조건(ALL, TITLE, CONTENT, NICKNAME)")
//                        param("order", "정렬 조건(LATEST, POPULAR, COMMENT, VIEW)")
//                        param("startDate", "시작 날짜(YYYY-MM-DD)")
//                        param("endDate", "종료 날짜(YYYY-MM-DD)")
//                        param("isMarked", "북마크 여부(Y, N)")
//                    }
//                    responseBody {
//                        field("data.result[0].boardId", "게시물 ID")
//                        field("data.result[0].nickname", "닉네임")
//                        field("data.result[0].title", "제목")
//                        field("data.result[0].content", "내용")
//                        field("data.result[0].viewCount", "조회수")
//                        field("data.result[0].createdAt", "작성일")
//                        field("data.result[0].isMarked", "북마크 여부")
//                        field("data.result[0].likeCount", "좋아요 수")
//                        field("data.result[0].tags[0]", "태그")
//                        field("data.totalElements", "총 게시물 수")
//                        field("data.totalPages", "총 페이지 수")
//                        field("data.currentPage", "현재 페이지")
//                        field("data.pageSize", "페이지 크기")
//                        field("status", "상태")
//                        field("statusCode", "상태 코드")
//                        field("timestamp", "응답 시간")
//                    }
//                    assertBuilder {
//                        assert("data.result[0].boardId", response.boardId)
//                        assert("data.result[0].nickname", response.nickname)
//                        assert("data.result[0].title", response.title)
//                        assert("data.result[0].content", response.content)
//                        assert("data.result[0].viewCount", response.viewCount)
//                        assert("data.result[0].isMarked", response.isMarked.toString())
//                        assert("data.result[0].likeCount", response.likeCount)
//                        assert("data.result[0].tags[0]", response.tags[0])
//                        assert("data.totalElements", pageResponse.totalElements)
//                        assert("data.totalPages", pageResponse.totalPages)
//                        assert("data.currentPage", pageResponse.currentPage)
//                        assert("data.pageSize", pageResponse.pageSize)
//                    }
//                }
//
////                mockMvc.perform(
////                    get("/api/v1/board/search")
////                        .param("page", "1")
////                        .param("query", "검색어")
////                        .param("condition", "ALL")
////                        .param("order", "VIEW")
////                        .param("startDate", "2025-01-01")
////                        .param("endDate", "2025-12-31")
////                        .param("isMarked", "Y")
////                        .contentType(MediaType.APPLICATION_JSON)
////                )
////                    .andExpect(status().isOk)
////                    .andExpect(jsonPath("data.result[0].boardId").value(response.boardId))
////                    .andExpect(jsonPath("data.result[0].nickname").value(response.nickname))
////                    .andExpect(jsonPath("data.result[0].title").value(response.title))
////                    .andExpect(jsonPath("data.result[0].content").value(response.content))
////                    .andExpect(jsonPath("data.result[0].viewCount").value(response.viewCount))
////                    .andExpect(jsonPath("data.result[0].isMarked").value(response.isMarked.toString()))
////                    .andExpect(jsonPath("data.result[0].likeCount").value(response.likeCount))
////                    .andExpect(jsonPath("data.result[0].tags[0]").value(response.tags[0]))
////                    .andExpect(jsonPath("data.totalElements").value(pageResponse.totalElements))
////                    .andExpect(jsonPath("data.totalPages").value(pageResponse.totalPages))
////                    .andExpect(jsonPath("data.currentPage").value(pageResponse.currentPage))
////                    .andExpect(jsonPath("data.pageSize").value(pageResponse.pageSize))
////                    .andDo(
////                        document(
////                            "성공",
////                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
////                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
////                            queryParameters(
////                                parameterWithName("page").description("페이지 번호"),
////                                parameterWithName("query").description("검색어"),
////                                parameterWithName("condition").description("검색 조건(ALL, TITLE, CONTENT, NICKNAME)"),
////                                parameterWithName("order").description("정렬 조건(LATEST, POPULAR, COMMENT, VIEW)"),
////                                parameterWithName("startDate").description("시작 날짜(YYYY-MM-DD)"),
////                                parameterWithName("endDate").description("종료 날짜(YYYY-MM-DD)"),
////                                parameterWithName("isMarked").description("북마크 여부(Y, N)")
////                            ),
////                            responseFields(
////                                fieldWithPath("data.result[0].boardId").description("게시물 ID"),
////                                fieldWithPath("data.result[0].nickname").description("닉네임"),
////                                fieldWithPath("data.result[0].title").description("제목"),
////                                fieldWithPath("data.result[0].content").description("내용"),
////                                fieldWithPath("data.result[0].viewCount").description("조회수"),
////                                fieldWithPath("data.result[0].createdAt").description("작성일"),
////                                fieldWithPath("data.result[0].isMarked").description("북마크 여부"),
////                                fieldWithPath("data.result[0].likeCount").description("좋아요 수"),
////                                fieldWithPath("data.result[0].tags[0]").description("태그"),
////                                fieldWithPath("data.totalElements").description("총 게시물 수"),
////                                fieldWithPath("data.totalPages").description("총 페이지 수"),
////                                fieldWithPath("data.currentPage").description("현재 페이지"),
////                                fieldWithPath("data.pageSize").description("페이지 크기"),
////                                fieldWithPath("status").description("상태"),
////                                fieldWithPath("statusCode").description("상태 코드"),
////                                fieldWithPath("timestamp").description("응답 시간"),
////                            ),
////                            resource(
////                                ResourceSnippetParameters.builder()
////                                    .tag("게시물")
////                                    .summary("게시물 목록 조회 API")
////                                    .build()
////                            )
////                        )
////                    )
//            }
//        }
//    }
//
//    describe("게시물 수정 API") {
//        val request = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
//            .set("tags", listOf("1234567890"))
//            .sample()
//
//        context("정상적인 요청인 경우") {
//            every { boardService.modifyBoard(request) } just Runs
//
//            it("성공") {
//                mockMvc.makeDocument("성공", "게시물", "게시물 수정 API") {
//                    requestLine(HttpMethod.PUT, "/api/v1/board") {
//                        content(objectMapper.writeValueAsString(request))
//                    }
//                    requestBody {
//                        field("boardId", "게시물 ID!!")
//                        field("title", "제목")
//                        field("content", "내용")
//                        field("tags", "태그")
//                        field("savedTagIds", "저장된 태그 ID")
//                        field("images", "이미지")
//                    }
//                    responseBody {
//                        field("status", "상태")
//                        field("statusCode", "상태 코드")
//                        field("timestamp", "응답 시간")
//                    }
//                }
//            }
//        }
//    }
//
//    describe("이미지 업로드 API") {
//        val file = MockMultipartFile("image", "test.png", "image/jpeg", "test".toByteArray())
//        val s3FileDto = fixtureMonkey.giveMeOne(S3FileDto::class.java)
//
//        context("정상적인 요청인 경우") {
//            every {
//                fileService.uploadAtchFile(any(), any())
//            } returns s3FileDto
//
//            it("성공") {
//                mockMvc.makeDocument("성공", "게시물", "이미지 업로드 API!!") {
//                    multipartRequestLine(HttpMethod.POST, "/api/v1/board/upload-image") {
//                        contentType(MediaType.MULTIPART_FORM_DATA)
//                        file(file)
//                    }
//                    assertBuilder(status().isOk) {
////                        status()
//                    }
//                    requestPartBody {
//                        field("image")
//                    }
//                    responseBody {
//                        field("data.atchFileDetailId", "첨부파일 상세 ID")
//                        field("data.atchFileUrl", "첨부파일 URL")
//                        field("status", "상태")
//                        field("statusCode", "상태 코드")
//                        field("timestamp", "응답 시간")
//                    }
//                }
//
//                mockMvc.perform(
//                    multipart(HttpMethod.POST, "/api/v1/board/upload-image")
//                        .file(file)
//                        .contentType(MediaType.MULTIPART_FORM_DATA)
//                )
//                    .andExpect(status().isOk)
//                    .andExpect(jsonPath("data.atchFileDetailId").value(s3FileDto.atchFileDetailId))
//                    .andExpect(jsonPath("data.atchFileUrl").value(s3FileDto.atchFileUrl))
//
//                    .andDo(
//                        document(
//                            "이미지 업로드 API - 성공",
//                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
//                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
//                            requestPartBody("image"),
//                            resource(
//                                ResourceSnippetParameters.builder()
//                                    .tag("게시물")
//                                    .summary("이미지 업로드 API")
//                                    .responseFields(
//                                        fieldWithPath("data.atchFileDetailId").description("첨부파일 상세 ID"),
//                                        fieldWithPath("data.atchFileUrl").description("첨부파일 URL"),
//                                        fieldWithPath("status").description("상태"),
//                                        fieldWithPath("statusCode").description("상태 코드"),
//                                        fieldWithPath("timestamp").description("응답 시간"),
//                                    )
//                                    .build()
//                            )
//                        )
//                    )
//            }
//        }
//    }
//})
