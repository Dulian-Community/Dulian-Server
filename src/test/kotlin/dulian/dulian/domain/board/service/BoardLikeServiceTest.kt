package dulian.dulian.domain.board.service

import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.entity.Board
import dulian.dulian.domain.board.entity.BoardLike
import dulian.dulian.domain.board.exception.BoardErrorCode
import dulian.dulian.domain.board.repository.BoardLikeRepository
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

class BoardLikeServiceTest : DescribeSpec({

    val boardLikeRepository: BoardLikeRepository = mockk()
    val memberRepository: MemberRepository = mockk()
    val boardRepository: BoardRepository = mockk()

    val boardLikeService = BoardLikeService(
        boardLikeRepository = boardLikeRepository,
        memberRepository = memberRepository,
        boardRepository = boardRepository
    )

    val fixtureMonkey = fixtureMonkey()

    describe("게시물 좋아요") {
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
                val exception = shouldThrow<CustomException> { boardLikeService.like(1L) }

                exception shouldBe CustomException(CommonErrorCode.UNAUTHORIZED)

                verify { memberRepository.findByIdOrNull(any()) }
            }
        }

        context("이미 좋아요를 누른 게시물인 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardLikeRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) } returns true

            it("exception") {
                val exception = shouldThrow<CustomException> { boardLikeService.like(1L) }

                exception shouldBe CustomException(BoardErrorCode.ALREADY_LIKED)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) }
            }
        }

        context("게시물 정보가 존재하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardLikeRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) } returns false
            every { boardRepository.findByIdOrNull(any()) } returns null

            it("exception") {
                val exception = shouldThrow<CustomException> { boardLikeService.like(1L) }

                exception shouldBe CustomException(BoardErrorCode.BOARD_NOT_FOUND)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardRepository.findByIdOrNull(any()) }
            }
        }

        context("정상적인 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardLikeRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) } returns false
            every { boardRepository.findByIdOrNull(any()) } returns board
            every { boardLikeRepository.save(any()) } returns mockk()

            it("게시물 좋아요") {
                boardLikeService.like(1L)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.existsByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.save(any()) }
            }
        }
    }

    describe("게시물 좋아요 취소") {
        val member = fixtureMonkey.giveMeBuilder<Member>()
            .setNotNull("memberId")
            .sample()
        val board = fixtureMonkey.giveMeBuilder<Board>()
            .setNotNull("boardId")
            .sample()
        val boardLike = BoardLike(
            board = board,
            member = member
        )

        context("회원 정보가 존재하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns null

            it("exception") {
                val exception = shouldThrow<CustomException> { boardLikeService.unlike(1L) }

                exception shouldBe CustomException(CommonErrorCode.UNAUTHORIZED)

                verify { memberRepository.findByIdOrNull(any()) }
            }
        }

        context("좋아요 정보가 존재하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardLikeRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) } returns null

            it("exception") {
                val exception = shouldThrow<CustomException> { boardLikeService.unlike(1L) }

                exception shouldBe CustomException(BoardErrorCode.BOARD_LIKE_NOT_FOUND)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) }
            }
        }

        context("정상적인 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardLikeRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) } returns boardLike
            every { boardLikeRepository.delete(any()) } returns mockk()

            it("게시물 좋아요 취소") {
                boardLikeService.unlike(1L)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardLikeRepository.delete(any()) }
            }
        }
    }
})