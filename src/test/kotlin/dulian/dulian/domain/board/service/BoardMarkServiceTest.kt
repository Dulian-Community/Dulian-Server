package dulian.dulian.domain.board.service

import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.entity.Board
import dulian.dulian.domain.board.entity.BoardMark
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
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
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
                val exception = shouldThrow<CustomException> { boardMarkService.mark(1L) }

                exception.errorCode shouldBe CommonErrorCode.UNAUTHORIZED

                verify { memberRepository.findByIdOrNull(any()) }
            }
        }

        context("이미 북마크한 게시물인 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardMarkRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) } returns true

            it("exception") {
                val exception = shouldThrow<CustomException> { boardMarkService.mark(1L) }

                exception.errorCode shouldBe BoardErrorCode.ALREADY_MARKED

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) }
            }
        }

        context("게시물 정보가 존재하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardMarkRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) } returns false
            every { boardRepository.findByIdOrNull(any()) } returns null

            it("exception") {
                val exception = shouldThrow<CustomException> { boardMarkService.mark(1L) }

                exception.errorCode shouldBe BoardErrorCode.BOARD_NOT_FOUND

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardRepository.findByIdOrNull(any()) }
            }
        }

        context("정상적인 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardMarkRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) } returns false
            every { boardRepository.findByIdOrNull(any()) } returns board
            every { boardMarkRepository.save(any()) } returns mockk()

            it("게시물 북마크") {
                boardMarkService.mark(1L)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.save(any()) }
            }
        }
    }

    describe("게시물 북마크 취소") {
        val member = fixtureMonkey.giveMeBuilder<Member>()
            .setNotNull("memberId")
            .sample()
        val board = fixtureMonkey.giveMeBuilder<Board>()
            .setNotNull("boardId")
            .sample()
        val boardMark = BoardMark(
            board = board,
            member = member
        )

        context("회원 정보가 존재하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns null

            it("exception") {
                val exception = shouldThrow<CustomException> { boardMarkService.unmark(1L) }

                exception.errorCode shouldBe CommonErrorCode.UNAUTHORIZED

                verify { memberRepository.findByIdOrNull(any()) }
            }
        }

        context("북마크 정보가 존재하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardMarkRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) } returns null

            it("exception") {
                val exception = shouldThrow<CustomException> { boardMarkService.unmark(1L) }

                exception.errorCode shouldBe BoardErrorCode.BOARD_MARK_NOT_FOUND

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) }
            }
        }

        context("정상적인 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardMarkRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) } returns boardMark
            every { boardMarkRepository.delete(any()) } returns mockk()

            it("게시물 북마크 취소") {
                boardMarkService.unmark(1L)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardMarkRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardMarkRepository.delete(any()) }
            }
        }
    }
})