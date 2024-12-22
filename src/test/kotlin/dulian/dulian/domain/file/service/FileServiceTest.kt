package dulian.dulian.domain.file.service

import dulian.dulian.domain.file.components.S3Utils
import dulian.dulian.domain.file.enums.S3Folder
import dulian.dulian.domain.file.exception.FileErrorCode
import dulian.dulian.domain.file.repository.AtchFileRepository
import dulian.dulian.global.exception.CustomException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.mock.web.MockMultipartFile

class FileServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val s3Utils: S3Utils = mockk()
    val atchFileRepository: AtchFileRepository = mockk()
    val imageExtensions = "jpg, jpeg, png, gif"

    val fileService = FileService(s3Utils, atchFileRepository, imageExtensions)

    Context("이미지 파일 다중 저장") {
        Given("허용되지 않는 확장자의 파일이 주어질 때") {
            val file = MockMultipartFile("file", "test.pdf", "image/jpeg", "test".toByteArray())

            When("이미지 파일 저장 시") {
                val exception = shouldThrow<CustomException> {
                    fileService.saveImageAtchFiles(
                        listOf(file),
                        S3Folder.GENERAL_BOARD
                    )
                }

                Then("exception") {
                    exception shouldBe CustomException(FileErrorCode.INVALID_FILE_EXTENSION)
                }
            }
        }

        Given("정상적인 파일이 주어진 경우") {
            val file = MockMultipartFile("file", "test.png", "image/jpeg", "test".toByteArray())

            every { atchFileRepository.save(any()) } returns mockk()
            every { s3Utils.uploadFile(any(), any(), any()) } returns Unit

            When("이미지 파일 저장 시") {
                fileService.saveImageAtchFiles(
                    listOf(file),
                    S3Folder.GENERAL_BOARD
                )

                Then("성공") {
                    verify { atchFileRepository.save(any()) }
                    verify { s3Utils.uploadFile(any(), any(), any()) }
                }
            }
        }
    }
})
