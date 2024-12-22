package dulian.dulian.domain.board.service

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.repository.BoardRepository
import dulian.dulian.domain.file.service.FileService
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.mock.web.MockMultipartFile

class BoardServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val boardRepository: BoardRepository = mockk()
    val memberRepository: MemberRepository = mockk()
    val fileService: FileService = mockk()

    val boardService = BoardService(boardRepository, memberRepository, fileService)

    val fixtureMonkey = FixtureMonkey.builder()
        .plugin(KotlinPlugin())
        .plugin(JakartaValidationPlugin())
        .build()

    Context("게시물 등록") {
        val request = fixtureMonkey.giveMeOne(GeneralBoardAddDto.Request::class.java)
        val member = fixtureMonkey.giveMeOne(Member::class.java)

        Given("사용자 정보가 존재하지 않을 경우") {
            every { memberRepository.findByUserId(any()) } returns null

            When("게시물 등록 시") {
                val exception = shouldThrow<CustomException> {
                    boardService.addBoard(request, emptyList())
                }

                Then("exception") {
                    exception shouldBe CustomException(CommonErrorCode.UNAUTHORIZED)

                    verify { memberRepository.findByUserId(any()) }
                }
            }
        }

        Given("정상적인 요청인 경우 - 첨부파일 X") {
            every { memberRepository.findByUserId(any()) } returns member
            every { boardRepository.save(any()) } returns mockk()

            When("게시물 등록 시") {
                boardService.addBoard(request, emptyList())

                Then("성공") {
                    verify { memberRepository.findByUserId(any()) }
                    verify { boardRepository.save(any()) }
                }
            }
        }

        Given("정상적인 요청인 경우 - 첨부파일 O") {
            val file = MockMultipartFile("file", "test.png", "image/jpeg", "test".toByteArray())

            every { memberRepository.findByUserId(any()) } returns member
            every { boardRepository.save(any()) } returns mockk()
            every { fileService.saveImageAtchFiles(any(), any()) } returns mockk()

            When("게시물 등록 시") {
                boardService.addBoard(request, listOf(file))

                Then("성공") {
                    verify { memberRepository.findByUserId(any()) }
                    verify { boardRepository.save(any()) }
                    verify { fileService.saveImageAtchFiles(any(), any()) }
                }
            }
        }
    }
})
