package dulian.dulian.domain.auth.service

import io.kotest.core.spec.style.BehaviorSpec

class SignupServiceTest : BehaviorSpec({

    Given("아이디가 중복일 때") {
        When("회원가입을 하면") {
            Then("exception") {
            }
        }
    }

    Given("닉네임이 중복일 때") {
        When("회원가입을 하면") {
            Then("Exception") {

            }
        }
    }

    Given("비밀번호가 일치하지 않을 때") {
        When("회원가입을 하면") {
            Then("Exception") {

            }
        }
    }

    Given("이메일 인증번호가 일치하지 않을 때") {
        When("회원가입을 하면") {
            Then("Exception") {

            }
        }
    }

    Given("정상적인 요청일 때") {
        When("회원가입을 하면") {
            Then("회원가입 성공") {

            }
        }
    }
})