package dulian.dulian.domain.board.service

import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.entity.Board
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.repository.BoardMarkRepository
import dulian.dulian.domain.board.repository.BoardRepository
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import dulian.dulian.utils.fixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.data.repository.findByIdOrNull

class BoardMarkServiceTest : DescribeSpec({

    val boardMarkRepository: BoardMarkRepository = mockk()
    val memberRepository: MemberRepository = mockk()
    val boardRepository: BoardRepository = mockk()

    val boardMarkService = BoardMarkService(
        boardMarkRepository = boardMarkRepository,
        memberRepository = memberRepository,
        boardRepository = boardRepository
    )

    val fixtureMonkey = fixtureMonkey()

    describe("게시물 북마크") {
        val member = fixtureMonkey.giveMeBuilder<Member>()
            .setNotNull("memberId")
            .sample()
        val board = fixtureMonkey.giveMeBuilder<Board>()
            .setNotNull("boardId")
            .sample()

        context("회원 정보가 존재하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns null

            it("exception") {
                val exception = shouldThrow<CustomException> { boardMarkService.toggleMark(1L) }

                exception.errorCode shouldBe CommonErrorCode.UNAUTHORIZED

                verify { memberRepository.findByIdOrNull(any()) }
            }
        }

        context("게시물 정보가 존재하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardRepository.findByIdOrNull(any()) } returns null

            it("exception") {
                val exception = shouldThrow<CustomException> { boardMarkService.toggleMark(1L) }

                exception.errorCode shouldBe BoardErrorCode.BOARD_NOT_FOUND

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardRepository.findByIdOrNull(any()) }
            }
        }

        context("북마크를 하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardMarkRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) } returns null
            every { boardRepository.findByIdOrNull(any()) } returns board
            every { boardMarkRepository.save(any()) } returns mockk()

            it("게시물 북마크") {
                boardMarkService.toggleMark(1L)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.save(any()) }
            }
        }

        context("이미 북마크를 한 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardMarkRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) } returns mockk()
            every { boardRepository.findByIdOrNull(any()) } returns board
            every { boardMarkRepository.delete(any()) } just Runs

            it("게시물 북마크") {
                boardMarkService.toggleMark(1L)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.delete(any()) }
            }
        }
    }
})
