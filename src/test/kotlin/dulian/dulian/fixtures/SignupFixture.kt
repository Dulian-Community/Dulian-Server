package dulian.dulian.fixtures

import dulian.dulian.domain.auth.dto.SignupConfirmDto
import dulian.dulian.domain.auth.dto.SignupDto

fun signupDtoFixture(
    passwordConfirm: String = "1234"
) = SignupDto.Request(
    userId = "test",
    email = "test@test.com",
    emailConfirmCode = "1234",
    password = "1234",
    passwordConfirm = passwordConfirm,
    nickname = "test"
)

fun sendEmailConfirmCodeFixture() = SignupConfirmDto.Request(
    email = "test@test.com"
)