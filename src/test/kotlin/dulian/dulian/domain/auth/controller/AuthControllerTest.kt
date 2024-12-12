package dulian.dulian.domain.auth.controller

import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.domain.auth.service.SignupService
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest(AuthController::class)
class AuthControllerTest constructor(
    @Autowired val mockMvc: MockMvc
) : BehaviorSpec({

    val signupService = mockk<SignupService>()

    Context("회원가입 API") {
        Given("회원가입 요청이 들어왔을 때") {
            val request = SignupDto.Request(
                userId = "test",
                email = "test@test.com",
                emailConfirmCode = "1234",
                password = "test",
                passwordConfirm = "test",
                nickname = "test",
            )
            every { signupService.signup(request) } just Runs

            When("회원가입을 하면") {
                val actions = mockMvc.perform(
                    RestDocumentationRequestBuilders.post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf)
                )


                Then("회원가입이 성공해야 한다") {
                }
            }
        }
    }
}) {
}