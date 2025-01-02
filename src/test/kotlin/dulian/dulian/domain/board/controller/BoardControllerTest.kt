package dulian.dulian.domain.board.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.board.dto.BoardDto
import dulian.dulian.domain.board.dto.BoardModifyDto
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.dto.SearchDto
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.service.BoardService
import dulian.dulian.domain.file.dto.S3FileDto
import dulian.dulian.domain.file.exception.FileErrorCode
import dulian.dulian.domain.file.service.FileService
import dulian.dulian.global.common.PageResponseDto
import dulian.dulian.global.exception.CustomException
import dulian.dulian.utils.fixtureMonkey
import dulian.dulian.utils.makeDocument
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
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

    val fixtureMonkey = fixtureMonkey()

    beforeEach { restDocumentation.beforeTest(javaClass, it.name.testName) }
    afterEach { restDocumentation.afterTest() }

    describe("게시물 등록 API") {
        val request = fixtureMonkey.giveMeBuilder(GeneralBoardAddDto.Request::class.java)
            .set("tags", listOf("1234567890"))
            .sample()
        val wrongTagRequest = fixtureMonkey.giveMeBuilder(GeneralBoardAddDto.Request::class.java)
            .set("tags", listOf("12345678901"))
            .sample()

        context("태그 글자수가 10보다 큰 경우") {
            it("실패") {
                mockMvc.makeDocument("게시물 등록 실패 - 태그 글자수가 10보다 큰 경우", "게시물", "게시물 등록 API") {
                    requestLine(HttpMethod.POST, "/api/v1/board") {
                        requestBody(objectMapper.writeValueAsString(wrongTagRequest))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", BoardErrorCode.TOO_LONG_TAG.message)
                    }
                }
            }
        }

        context("정상적인 요청인 경우") {
            every { boardService.addBoard(request) } just Runs

            it("성공") {
                mockMvc.makeDocument("게시물 등록 성공 - 정상적인 요청인 경우", "게시물", "게시물 등록 API") {
                    requestLine(HttpMethod.POST, "/api/v1/board") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    requestBody {
                        field("title", "제목")
                        field("content", "내용")
                        field("tags", "태그")
                        field("images", "이미지")
                    }
                }
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
                mockMvc.makeDocument("이미지 업로드 실패 - 허용된 확장자가 아닌 경우", "게시물", "이미지 업로드 API") {
                    multipartRequestLine(HttpMethod.POST, "/api/v1/board/upload-image") {
                        file(file)
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", FileErrorCode.INVALID_FILE_EXTENSION.message)
                    }
                }
            }
        }

        context("정상적인 요청인 경우") {
            every {
                fileService.uploadAtchFile(any(), any())
            } returns s3FileDto

            it("성공") {
                mockMvc.makeDocument("이미지 업로드 성공 - 정상적인 요청인 경우", "게시물", "이미지 업로드 API") {
                    multipartRequestLine(HttpMethod.POST, "/api/v1/board/upload-image") {
                        file(file)
                    }
                    assertBuilder {
                        assert("data.atchFileDetailId", s3FileDto.atchFileDetailId)
                        assert("data.atchFileUrl", s3FileDto.atchFileUrl)
                    }
                    requestPartBody {
                        field("image")
                    }
                    responseBody {
                        field("data.atchFileDetailId", "첨부파일 상세 ID")
                        field("data.atchFileUrl", "첨부파일 URL")
                        field("status", "상태")
                        field("statusCode", "상태 코드")
                        field("timestamp", "응답 시간")
                    }
                }
            }
        }
    }

    describe("게시물 상세 조회 API") {
        val board = fixtureMonkey.giveMeBuilder(BoardDto::class.java)
            .set("boardId", 1L)
            .set("images", listOf(BoardDto.AtchFileDetailsDto(1L, "test.png", "test")))
            .set("tags", listOf(BoardDto.Tag(1L, "tag")))
            .sample()

        context("정상적인 요청인 경우") {
            every { boardService.getBoard(any()) } returns board

            it("게시물 상세 조회 결과 반환") {
                mockMvc.makeDocument("게시물 상세 조회 성공 - 정상적인 요청인 경우", "게시물", "게시물 상세 조회 API") {
                    requestLine(HttpMethod.GET, "/api/v1/board/{boardId}") {
                        pathVariable(1L)
                    }
                    pathParameters {
                        field("boardId", "게시물 ID")
                    }
                    assertBuilder {
                        assert("data.boardId", board.boardId)
                        assert("data.title", board.title)
                        assert("data.content", board.content)
                        assert("data.nickname", board.nickname)
                        assert("data.viewCount", board.viewCount)
                        assert("data.likeCount", board.likeCount)
                        assert("data.isLiked", board.isLiked.toString())
                        assert("data.isMarked", board.isMarked.toString())
                        assert("data.isMine", board.isMine.toString())
                        assert("data.images[0].imageId", board.images?.get(0)?.imageId)
                        assert("data.images[0].imageUrl", board.images?.get(0)?.imageUrl)
                        assert("data.tags[0].tagId", board.tags?.get(0)?.tagId)
                        assert("data.tags[0].name", board.tags?.get(0)?.name)
                    }
                    responseBody {
                        field("data.boardId", "게시물 ID")
                        field("data.title", "제목")
                        field("data.content", "내용")
                        field("data.nickname", "닉네임")
                        field("data.viewCount", "조회수")
                        field("data.likeCount", "좋아요 수")
                        field("data.isLiked", "좋아요 여부")
                        field("data.isMarked", "북마크 여부")
                        field("data.isMine", "본인 게시물 여부")
                        field("data.createdAt", "작성일")
                        field("data.images[0].imageId", "이미지 ID")
                        field("data.images[0].imageUrl", "이미지 URL")
                        field("data.tags[0].tagId", "태그 ID")
                        field("data.tags[0].name", "태그 이름")
                        field("status", "상태")
                        field("statusCode", "상태 코드")
                        field("timestamp", "응답 시간")
                    }
                }
            }
        }

        context("게시물이 존재하지 않는 경우") {
            every { boardService.getBoard(any()) } throws CustomException(BoardErrorCode.BOARD_NOT_FOUND)

            it("에러 코드 반환") {
                mockMvc.makeDocument("게시물 상세 조회 실패 - 게시물이 존재하지 않는 경우", "게시물", "게시물 상세 조회 API") {
                    requestLine(HttpMethod.GET, "/api/v1/board/{boardId}") {
                        pathVariable(1L)
                    }
                    pathParameters {
                        field("boardId", "게시물 ID")
                    }
                    assertBuilder(status().isNotFound) {
                        assert("message", BoardErrorCode.BOARD_NOT_FOUND.message)
                    }
                }
            }
        }
    }

    describe("게시물 수정 API") {
        val request = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
            .set("tags", listOf("1234567890"))
            .sample()

        context("태그 개수가 5개 초과인 경우") {
            val wrongRequest = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
                .set("tags", listOf("1", "2", "3", "4", "5", "6"))
                .sample()

            it("실패") {
                mockMvc.makeDocument("게시물 수정 실패 - 태그 개수가 5개 초과인 경우", "게시물", "게시물 수정 API") {
                    requestLine(HttpMethod.PUT, "/api/v1/board") {
                        requestBody(objectMapper.writeValueAsString(wrongRequest))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", BoardErrorCode.TOO_MANY_5_TAGS.message)
                    }
                }
            }
        }

        context("태그 글자수가 10보다 큰 경우") {
            val wrongRequest = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
                .set("tags", listOf("12345678901"))
                .sample()

            it("실패") {
                mockMvc.makeDocument("게시물 수정 실패 - 태그 글자수가 10보다 큰 경우", "게시물", "게시물 수정 API") {
                    requestLine(HttpMethod.PUT, "/api/v1/board") {
                        requestBody(objectMapper.writeValueAsString(wrongRequest))
                    }
                    assertBuilder(status().isBadRequest) {
                        assert("message", BoardErrorCode.TOO_LONG_TAG.message)
                    }
                }
            }
        }

        context("정상적인 요청인 경우") {
            every { boardService.modifyBoard(request) } just Runs

            it("성공") {
                mockMvc.makeDocument("게시물 수정 성공 - 정상적인 요청인 경우", "게시물", "게시물 수정 API") {
                    requestLine(HttpMethod.PUT, "/api/v1/board") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    requestBody {
                        field("boardId", "게시물 ID")
                        field("title", "제목")
                        field("content", "내용")
                        field("tags", "태그")
                        field("savedTagIds", "저장된 태그 ID")
                        field("images", "이미지")
                    }
                }
            }
        }

        context("게시물이 존재하지 않거나 본인의 게시물이 아닌 경우") {
            every { boardService.modifyBoard(any()) } throws CustomException(BoardErrorCode.BOARD_NOT_FOUND)

            it("에러 코드 반환") {
                mockMvc.makeDocument("게시물 수정 실패 - 게시물이 존재하지 않거나 본인의 게시물이 아닌 경우", "게시물", "게시물 수정 API") {
                    requestLine(HttpMethod.PUT, "/api/v1/board") {
                        requestBody(objectMapper.writeValueAsString(request))
                    }
                    assertBuilder(status().isNotFound) {
                        assert("message", BoardErrorCode.BOARD_NOT_FOUND.message)
                    }
                }
            }
        }
    }

    describe("게시물 삭제 API") {
        val boardId = 1L
        context("정상적인 요청인 경우") {
            every { boardService.removeBoard(any()) } just Runs

            it("성공") {
                mockMvc.makeDocument("게시물 삭제 성공 - 정상적인 요청인 경우", "게시물", "게시물 삭제 API") {
                    requestLine(HttpMethod.DELETE, "/api/v1/board/{boardId}") {
                        pathVariable(boardId)
                    }
                    pathParameters {
                        field("boardId", "게시물 ID")
                    }
                }
            }
        }

        context("게시물이 존재하지 않거나 본인의 게시물이 아닌 경우") {
            every { boardService.removeBoard(any()) } throws CustomException(BoardErrorCode.BOARD_NOT_FOUND)

            it("에러 코드 반환") {
                mockMvc.makeDocument("게시물 삭제 실패 - 게시물이 존재하지 않거나 본인의 게시물이 아닌 경우", "게시물", "게시물 삭제 API") {
                    requestLine(HttpMethod.DELETE, "/api/v1/board/{boardId}") {
                        pathVariable(boardId)
                    }
                    assertBuilder(status().isNotFound) {
                        assert("message", BoardErrorCode.BOARD_NOT_FOUND.message)
                    }
                }
            }
        }
    }

    describe("게시물 목록 조회 API") {
        val request = SearchDto.Request(
            page = 1,
            query = "검색어",
            condition = "ALL",
            order = "VIEW",
            startDate = "2025-01-01",
            endDate = "2025-12-31",
            isMarked = "Y"
        )
        val response = fixtureMonkey.giveMeOne<SearchDto.Response>()
        response.tags = listOf("tag1", "tag2")

        val pageResponse = PageResponseDto<SearchDto.Response>(
            result = listOf(response),
            totalElements = 1,
            totalPages = 1,
            currentPage = 1,
            pageSize = 10
        )
        context("정상적인 요청인 경우") {
            every { boardService.search(request) } returns pageResponse

            it("게시물 목록 반환") {
                mockMvc.makeDocument("게시물 목록 조회 성공 - 정상적인 요청인 경우", "게시물", "게시물 목록 조회 API") {
                    requestLine(HttpMethod.GET, "/api/v1/board/search") {
                        param("page", "1")
                        param("query", "검색어")
                        param("condition", "ALL")
                        param("order", "VIEW")
                        param("startDate", "2025-01-01")
                        param("endDate", "2025-12-31")
                        param("isMarked", "Y")
                    }
                    assertBuilder {
                        assert("data.result[0].boardId", response.boardId)
                        assert("data.result[0].nickname", response.nickname)
                        assert("data.result[0].title", response.title)
                        assert("data.result[0].content", response.content)
                        assert("data.result[0].viewCount", response.viewCount)
                        assert("data.result[0].isMarked", response.isMarked.toString())
                        assert("data.result[0].likeCount", response.likeCount)
                        assert("data.result[0].tags[0]", response.tags[0])
                        assert("data.totalElements", pageResponse.totalElements)
                        assert("data.totalPages", pageResponse.totalPages)
                        assert("data.currentPage", pageResponse.currentPage)
                        assert("data.pageSize", pageResponse.pageSize)
                    }
                    queryParameters {
                        param("page", "페이지 번호")
                        param("query", "검색어")
                        param("condition", "검색 조건(ALL, TITLE, CONTENT, NICKNAME)")
                        param("order", "정렬 조건(LATEST, POPULAR, COMMENT, VIEW)")
                        param("startDate", "시작 날짜(YYYY-MM-DD)")
                        param("endDate", "종료 날짜(YYYY-MM-DD)")
                        param("isMarked", "북마크 여부(Y, N)")
                    }

                    responseBody {
                        field("data.result[0].boardId", "게시물 ID")
                        field("data.result[0].nickname", "닉네임")
                        field("data.result[0].title", "제목")
                        field("data.result[0].content", "내용")
                        field("data.result[0].viewCount", "조회수")
                        field("data.result[0].createdAt", "작성일")
                        field("data.result[0].isMarked", "북마크 여부")
                        field("data.result[0].likeCount", "좋아요 수")
                        field("data.result[0].tags[0]", "태그")
                        field("data.totalElements", "총 게시물 수")
                        field("data.totalPages", "총 페이지 수")
                        field("data.currentPage", "현재 페이지")
                        field("data.pageSize", "페이지 크기")
                        field("status", "상태")
                        field("statusCode", "상태 코드")
                        field("timestamp", "응답 시간")
                    }
                }
            }
        }
    }
})
