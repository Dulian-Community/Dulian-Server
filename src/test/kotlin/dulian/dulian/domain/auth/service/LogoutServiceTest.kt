package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.repository.RefreshTokenRepository
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import org.springframework.mock.web.MockHttpServletResponse

class LogoutServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val refreshTokenRepository: RefreshTokenRepository = mockk()

    val logoutService = LogoutService(refreshTokenRepository)

    Context("로그아웃") {
        val response = MockHttpServletResponse()

        Given("정상적인 경우") {
            every { refreshTokenRepository.deleteByUserId(any()) } just Runs

            When("로그아웃을 하면") {
                logoutService.logout(response)

                Then("로그아웃 성공") {
                    verify { refreshTokenRepository.deleteByUserId(any()) }
                }
            }
        }
    }
})
