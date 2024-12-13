package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.auth.repository.RefreshTokenRepository
import dulian.dulian.global.auth.jwt.components.JwtTokenProvider
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.mockk
import org.springframework.security.crypto.password.PasswordEncoder

class LoginServiceTest : BehaviorSpec({
    isolationMode = IsolationMode.InstancePerLeaf

    val memberRepository: MemberRepository = mockk()
    val passwordEncoder: PasswordEncoder = mockk()
    val jwtTokenProvider: JwtTokenProvider = mockk()
    val refreshTokenRepository: RefreshTokenRepository = mockk()

    val loginService = LoginService(
        memberRepository,
        passwordEncoder,
        jwtTokenProvider,
        refreshTokenRepository
    )

    Context("로그인") {
//        val monkeyFixture = MonkeyFix
        Given("ID가 존재하지 않을 때") {

            When("로그인을 하면") {

                Then("exception") {

                }
            }
        }
    }
})