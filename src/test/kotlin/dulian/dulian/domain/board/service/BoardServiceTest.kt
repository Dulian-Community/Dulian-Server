package dulian.dulian.domain.board.service

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.dto.BoardDto
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.repository.BoardRepository
import dulian.dulian.domain.board.repository.TagRepository
import dulian.dulian.domain.file.entity.AtchFile
import dulian.dulian.domain.file.entity.AtchFileDetail
import dulian.dulian.domain.file.repository.AtchFileDetailRepository
import dulian.dulian.domain.file.repository.AtchFileRepository
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*

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
})
