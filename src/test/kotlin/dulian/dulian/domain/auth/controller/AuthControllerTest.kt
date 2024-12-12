package dulian.dulian.domain.auth.controller

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.domain.auth.service.LoginService
import dulian.dulian.domain.auth.service.LogoutService
import dulian.dulian.domain.auth.service.SignupService
import dulian.dulian.domain.auth.service.TokenRefreshService
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.restdocs.ManualRestDocumentation
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.request
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@WebMvcTest(AuthController::class)
@ExtendWith(RestDocumentationExtension::class)
class AuthControllerTest(
    @MockkBean
    private val signupService: SignupService,

    @MockkBean
    private val loginService: LoginService,

    @MockkBean
    private val tokenRefreshService: TokenRefreshService,

    @MockkBean
    private val logoutService: LogoutService,

    @Autowired
    private val context: WebApplicationContext
) : DescribeSpec({

    val restDocumentation = ManualRestDocumentation()
    val mockMVc = MockMvcBuilders.webAppContextSetup(context)
        .apply<DefaultMockMvcBuilder>(
            MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
                .operationPreprocessors()
                .withRequestDefaults(Preprocessors.prettyPrint())
                .withResponseDefaults(Preprocessors.prettyPrint())
        )
//		.apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity(MockSecurityFilter()))
//		.addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
        .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
        .build()

    val objectMapper = ObjectMapper()

    beforeEach { restDocumentation.beforeTest(javaClass, it.name.testName) }
    afterEach { restDocumentation.afterTest() }

    describe("Test 1") {

        context("Test 2") {

            val request = SignupDto.Request(
                userId = "test12345",
                email = "test@test.com",
                emailConfirmCode = "1234",
                password = "Test1234!",
                passwordConfirm = "Test1234!",
                nickname = "test"
            )

            every { signupService.signup(request) } just Runs

            it("Test 3") {
//                mockMVc.post("/api/v1/auth/signup") {
//                    contentType = MediaType.APPLICATION_JSON
//                    content = objectMapper.writeValueAsString(request)
//                }.andExpect {
//                    status { isOk() }
//                }.andDo {
//                    document(
//                        "signup",
//                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
//                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
//                    )
//                }

                mockMVc.perform(
                    request(HttpMethod.POST, "/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                    .andExpect(status().isOk)
                    .andDo(
                        document(
                            "signup",
                            Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                            Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
                        )
                    )
            }
        }
    }
})
