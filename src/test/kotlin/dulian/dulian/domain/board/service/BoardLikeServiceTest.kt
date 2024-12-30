package dulian.dulian.domain.board.service

import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.entity.Board
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
import io.mockk.*
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

    describe("게시물 좋아요/좋아요 취소") {
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
                val exception = shouldThrow<CustomException> { boardLikeService.toggleLike(1L) }

                exception shouldBe CustomException(CommonErrorCode.UNAUTHORIZED)

                verify { memberRepository.findByIdOrNull(any()) }
            }
        }

        context("게시물 정보가 존재하지 않는 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardRepository.findByIdOrNull(any()) } returns null

            it("exception") {
                val exception = shouldThrow<CustomException> { boardLikeService.toggleLike(1L) }

                exception shouldBe CustomException(BoardErrorCode.BOARD_NOT_FOUND)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardRepository.findByIdOrNull(any()) }
            }
        }

        context("좋아요를 누른 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardLikeRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) } returns mockk()
            every { boardRepository.findByIdOrNull(any()) } returns board
            every { boardLikeRepository.delete(any()) } just Runs

            it("좋아요 취소") {
                boardLikeService.toggleLike(1L)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.delete(any()) }
            }
        }

        context("좋아요를 누르지 않은 경우") {
            mockkObject(SecurityUtils)
            every { SecurityUtils.getCurrentUserId() } returns 1L
            every { memberRepository.findByIdOrNull(any()) } returns member
            every { boardLikeRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) } returns null
            every { boardRepository.findByIdOrNull(any()) } returns board
            every { boardLikeRepository.save(any()) } returns mockk()

            it("좋아요 취소") {
                boardLikeService.toggleLike(1L)

                verify { memberRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.findByBoardBoardIdAndMemberMemberId(any(), any()) }
                verify { boardRepository.findByIdOrNull(any()) }
                verify { boardLikeRepository.save(any()) }
            }
        }
    }
})
