package dulian.dulian.domain.board.service

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.dto.BoardDto
import dulian.dulian.domain.board.dto.BoardModifyDto
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.entity.Board
import dulian.dulian.domain.board.entity.Tag
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.repository.BoardRepository
import dulian.dulian.domain.board.repository.TagRepository
import dulian.dulian.domain.file.entity.AtchFile
import dulian.dulian.domain.file.entity.AtchFileDetail
import dulian.dulian.domain.file.repository.AtchFileDetailRepository
import dulian.dulian.domain.file.repository.AtchFileRepository
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.data.repository.findByIdOrNull

class BoardServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val boardRepository: BoardRepository = mockk()
    val memberRepository: MemberRepository = mockk()
    val atchFileRepository: AtchFileRepository = mockk()
    val atchFileDetailRepository: AtchFileDetailRepository = mockk()
    val tagRepository: TagRepository = mockk()

    val boardService =
        BoardService(
            boardRepository,
            memberRepository,
            atchFileRepository,
            atchFileDetailRepository,
            tagRepository,
            "s3Url"
        )

    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JakartaValidationPlugin())
        .build()

    Context("게시물 등록") {
        val request = fixtureMonkey.giveMeBuilder(GeneralBoardAddDto.Request::class.java)
            .set("images", listOf(1L))
            .set("tags", listOf("tag"))
            .sample()
        val member = fixtureMonkey.giveMeOne(Member::class.java)
        val atchFile = fixtureMonkey.giveMeBuilder(AtchFile::class.java)
            .set("atchFileId", 1L)
            .sample()
        val atchFileDetail = fixtureMonkey.giveMeBuilder(AtchFileDetail::class.java)
            .set("atchFile", atchFile)
            .sample()
        val clearAtchFileDetail = fixtureMonkey.giveMeBuilder(AtchFileDetail::class.java)
            .set("atchFile", null)
            .sample()

        Given("사용자 정보가 존재하지 않을 경우") {
            every { memberRepository.findByUserId(any()) } returns null

            When("게시물 등록 시") {
                val exception = shouldThrow<CustomException> {
                    boardService.addBoard(request)
                }

                Then("exception") {
                    exception shouldBe CustomException(CommonErrorCode.UNAUTHORIZED)

                    verify { memberRepository.findByUserId(any()) }
                }
            }
        }

        Given("이미지 개수가 맞지 않는 경우") {
            every { memberRepository.findByUserId(any()) } returns member
            every { atchFileDetailRepository.findByAtchFileDetailIdIn(any()) } returns listOf(atchFileDetail)

            When("게시물 등록 시") {
                val exception = shouldThrow<CustomException> { boardService.addBoard(request) }

                Then("exception") {
                    exception shouldBe CustomException(CommonErrorCode.INVALID_PARAMETER)

                    verify { memberRepository.findByUserId(any()) }
                    verify { atchFileDetailRepository.findByAtchFileDetailIdIn(any()) }
                }
            }
        }

        Given("정상적인 요청인 경우") {
            every { memberRepository.findByUserId(any()) } returns member
            every { atchFileDetailRepository.findByAtchFileDetailIdIn(any()) } returns listOf(clearAtchFileDetail)
            every { boardRepository.save(any()) } returns mockk()
            every { atchFileRepository.save(any()) } returns atchFile
            every { tagRepository.save(any()) } returns mockk()
            every { atchFileDetailRepository.updateAtchFileDetails(any(), any()) } just Runs

            When("게시물 등록 시") {
                boardService.addBoard(request)

                Then("성공") {
                    verify { memberRepository.findByUserId(any()) }
                    verify { atchFileDetailRepository.findByAtchFileDetailIdIn(any()) }
                    verify { boardRepository.save(any()) }
                    verify { atchFileRepository.save(any()) }
                    verify { tagRepository.save(any()) }
                    verify { atchFileDetailRepository.updateAtchFileDetails(any(), any()) }
                }
            }
        }
    }

    Context("게시물 상세 조회") {
        val boardId = 1L
        val board = fixtureMonkey.giveMeOne(BoardDto::class.java)

        Given("게시물이 존재하지 않는 경우") {
            every { boardRepository.getBoard(any()) } returns null

            When("게시물 상세 조회 시") {
                val exception = shouldThrow<CustomException> { boardService.getBoard(boardId) }

                Then("exception") {
                    exception shouldBe CustomException(BoardErrorCode.BOARD_NOT_FOUND)

                    verify { boardRepository.getBoard(any()) }
                }
            }
        }

        Given("정상적인 요청인 경우") {
            every { boardRepository.getBoard(any()) } returns board
            every { boardRepository.increaseViewCount(any()) } just Runs

            When("게시물 상세 조회 시") {
                val result = boardService.getBoard(boardId)

                Then("exception") {
                    result shouldBe board

                    verify { boardRepository.getBoard(any()) }
                    verify { boardRepository.increaseViewCount(any()) }
                }
            }
        }
    }

    Context("게시물 수정") {
        val request = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
            .set("savedTagIds", listOf(3L))
            .set("tags", listOf("tag1", "tag2", "tag3"))
            .set("images", null)
            .sample()
        val member = fixtureMonkey.giveMeBuilder(Member::class.java)
            .set("memberId", 1L)
            .sample()
        val anyBoard = fixtureMonkey.giveMeBuilder(Board::class.java)
            .set("boardId", 1L)
            .set("member", member)
            .sample()
        val tag1 = Tag(1L, "tag", anyBoard)
        val tag2 = Tag(2L, "tag", anyBoard)
        val tag3 = Tag(3L, "tag", anyBoard)
        val atchFileDetail = fixtureMonkey.giveMeBuilder(AtchFileDetail::class.java)
            .set("atchFileDetailId", 1L)
            .sample()
        val atchFile = fixtureMonkey.giveMeBuilder(AtchFile::class.java)
            .set("atchFileDetails", listOf(atchFileDetail))
            .sample()
        val savedBoardWithTag = fixtureMonkey.giveMeBuilder(Board::class.java)
            .set("member", member)
            .set("tags", setOf(tag1, tag2, tag3))
            .set("atchFile", null)
            .sample()
        val savedBoardWithImage = fixtureMonkey.giveMeBuilder(Board::class.java)
            .set("member", member)
            .set("tags", null)
            .set("atchFile", atchFile)
            .sample()

        Given("게시물이 존재하지 않는 경우") {
            every { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) } returns null

            When("게시물 수정 시") {
                val exception = shouldThrow<CustomException> { boardService.modifyBoard(request) }

                Then("exception") {
                    exception shouldBe CustomException(BoardErrorCode.BOARD_NOT_FOUND)

                    verify { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) }
                }
            }
        }

        Given("본인의 게시물이 아닌 경우") {
            val wrongMember = fixtureMonkey.giveMeBuilder(Member::class.java)
                .set("memberId", 2L)
                .sample()
            mockkObject(SecurityUtils)
            every { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) } returns savedBoardWithTag
            every { SecurityUtils.getCurrentUserId() } returns "1"
            every { memberRepository.findByUserId(any()) } returns wrongMember

            When("게시물 수정 시") {
                val exception = shouldThrow<CustomException> { boardService.modifyBoard(request) }

                Then("exception") {
                    exception shouldBe CustomException(BoardErrorCode.BOARD_NOT_FOUND)

                    verify { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) }
                    verify { memberRepository.findByUserId(any()) }
                }
            }
        }

        Given("저장된 태그가 3개일 때 삭제하지 않는 경우") {
            val requestForTag = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
                .set("savedTagIds", listOf(1L, 2L, 3L))
                .set("tags", null)
                .set("images", null)
                .sample()

            mockkObject(SecurityUtils)
            every { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) } returns savedBoardWithTag
            every { SecurityUtils.getCurrentUserId() } returns "1"
            every { boardRepository.save(any()) } returns mockk()
            every { memberRepository.findByUserId(any()) } returns member

            When("게시물 수정 시") {
                boardService.modifyBoard(requestForTag)

                Then("성공") {
                    verify { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) }
                    verify(exactly = 0) { tagRepository.deleteTagByTagIds(any()) }
                    verify { boardRepository.save(any()) }
                }
            }
        }

        Given("저장된 태그가 3개일 때 2개를 삭제하고, 3개를 새롭게 저장하는 경우") {
            mockkObject(SecurityUtils)
            every { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) } returns savedBoardWithTag
            every { SecurityUtils.getCurrentUserId() } returns "1"
            every { tagRepository.deleteTagByTagIds(any()) } just Runs
            every { tagRepository.save(any()) } returns mockk()
            every { boardRepository.save(any()) } returns mockk()
            every { memberRepository.findByUserId(any()) } returns member

            When("게시물 수정 시") {
                boardService.modifyBoard(request)

                Then("성공") {
                    verify { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) }
                    verify { tagRepository.deleteTagByTagIds(any()) }
                    verify(exactly = 3) { tagRepository.save(any()) }
                    verify { boardRepository.save(any()) }
                }
            }
        }

        Given("저장된 이미지가 1개일 때 삭제하지 않는 경우") {
            val requestWithImage = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
                .set("savedTagIds", null)
                .set("tags", null)
                .set("images", listOf(1L))
                .sample()

            mockkObject(SecurityUtils)
            every { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) } returns savedBoardWithImage
            every { SecurityUtils.getCurrentUserId() } returns "1"
            every { boardRepository.save(any()) } returns mockk()
            every { memberRepository.findByUserId(any()) } returns member

            When("게시물 수정 시") {
                boardService.modifyBoard(requestWithImage)

                Then("성공") {
                    verify { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) }
                    verify { boardRepository.save(any()) }
                    verify(exactly = 0) { atchFileDetailRepository.deleteAtchFileDetailByAtchFileDetailIds(any()) }
                }
            }
        }

        Given("저장된 이미지가 1개일 때 삭제하고, 새로운 이미지 1개를 저장하는 경우") {
            val requestWithImage = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
                .set("savedTagIds", null)
                .set("tags", null)
                .set("images", listOf(2L))
                .sample()
            val clearAtchFileDetail = fixtureMonkey.giveMeBuilder(AtchFileDetail::class.java)
                .set("atchFile", null)
                .sample()

            mockkObject(SecurityUtils)
            every { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) } returns savedBoardWithImage
            every { SecurityUtils.getCurrentUserId() } returns "1"
            every { boardRepository.save(any()) } returns mockk()
            every { atchFileDetailRepository.deleteAtchFileDetailByAtchFileDetailIds(any()) } just Runs
            every { atchFileDetailRepository.findByAtchFileDetailIdIn(any()) } returns listOf(clearAtchFileDetail)
            every { atchFileDetailRepository.updateAtchFileDetails(any(), any()) } just Runs
            every { memberRepository.findByUserId(any()) } returns member

            When("게시물 수정 시") {
                boardService.modifyBoard(requestWithImage)

                Then("성공") {
                    verify { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) }
                    verify { boardRepository.save(any()) }
                    verify { atchFileDetailRepository.deleteAtchFileDetailByAtchFileDetailIds(any()) }
                    verify { atchFileDetailRepository.findByAtchFileDetailIdIn(any()) }
                    verify { atchFileDetailRepository.updateAtchFileDetails(any(), any()) }
                }
            }
        }

        Given("저장된 이미지가 없고, 새로운 이미지 1개를 저장하는 경우") {
            val requestWithImage = fixtureMonkey.giveMeBuilder(BoardModifyDto.Request::class.java)
                .set("savedTagIds", listOf(1L, 2L, 3L))
                .set("tags", null)
                .set("images", listOf(1L))
                .sample()
            val clearAtchFileDetail = fixtureMonkey.giveMeBuilder(AtchFileDetail::class.java)
                .set("atchFile", null)
                .sample()

            mockkObject(SecurityUtils)
            every { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) } returns savedBoardWithTag
            every { SecurityUtils.getCurrentUserId() } returns "1"
            every { boardRepository.save(any()) } returns mockk()
            every { atchFileDetailRepository.findByAtchFileDetailIdIn(any()) } returns listOf(clearAtchFileDetail)
            every { atchFileRepository.save(any()) } returns atchFile
            every { atchFileDetailRepository.updateAtchFileDetails(any(), any()) } just Runs
            every { memberRepository.findByUserId(any()) } returns member

            When("게시물 수정 시") {
                boardService.modifyBoard(requestWithImage)

                Then("성공") {
                    verify { boardRepository.findBoardAndTagsAndAtchFileAndAtchFileDetailsByBoardId(any()) }
                    verify { boardRepository.save(any()) }
                    verify { atchFileDetailRepository.findByAtchFileDetailIdIn(any()) }
                    verify { atchFileRepository.save(any()) }
                    verify { atchFileDetailRepository.updateAtchFileDetails(any(), any()) }
                }
            }
        }
    }

    Context("게시물 삭제") {
        val boardId = 1L
        val member = fixtureMonkey.giveMeBuilder(Member::class.java)
            .set("memberId", 1L)
            .sample()
        val savedBoard = fixtureMonkey.giveMeBuilder(Board::class.java)
            .set("member", member)
            .sample()

        Given("게시물이 존재하지 않는 경우") {
            every { boardRepository.findByIdOrNull(any()) } returns null

            When("게시물 삭제 시") {
                val exception = shouldThrow<CustomException> { boardService.removeBoard(boardId) }

                Then("exception") {
                    exception shouldBe CustomException(BoardErrorCode.BOARD_NOT_FOUND)

                    verify { boardRepository.findByIdOrNull(any()) }
                }
            }
        }

        Given("본인의 게시물이 아닌 경우") {
            val wrongMember = fixtureMonkey.giveMeBuilder(Member::class.java)
                .set("memberId", 2L)
                .sample()
            mockkObject(SecurityUtils)
            every { boardRepository.findByIdOrNull(any()) } returns savedBoard
            every { SecurityUtils.getCurrentUserId() } returns "1"
            every { memberRepository.findByUserId(any()) } returns wrongMember

            When("게시물 삭제 시") {
                val exception = shouldThrow<CustomException> { boardService.removeBoard(boardId) }

                Then("exception") {
                    exception shouldBe CustomException(BoardErrorCode.BOARD_NOT_FOUND)

                    verify { boardRepository.findByIdOrNull(any()) }
                }
            }
        }

        Given("정상적인 요청인 경우") {
            mockkObject(SecurityUtils)
            every { boardRepository.findByIdOrNull(any()) } returns savedBoard
            every { SecurityUtils.getCurrentUserId() } returns "1"
            every { memberRepository.findByUserId(any()) } returns member
            every { boardRepository.delete(any()) } just Runs

            When("게시물 삭제 시") {
                boardService.removeBoard(boardId)

                Then("성공") {
                    verify { boardRepository.findByIdOrNull(any()) }
                    verify { boardRepository.delete(any()) }
                }
            }
        }
    }
})
