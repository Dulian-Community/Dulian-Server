package dulian.dulian.domain.auth.exception

import dulian.dulian.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class SignupErrorCode(
    override val httpStatus: HttpStatus,
    override val errorCode: String,
    override val message: String,
) : ErrorCode {

    EXISTED_USER_ID(HttpStatus.BAD_REQUEST, "SU001", "이미 존재하는 아이디입니다."),
    EXISTED_NICKNAME(HttpStatus.BAD_REQUEST, "SU002", "이미 존재하는 닉네임입니다."),
    PASSWORD_CONFIRM_FAIL(HttpStatus.BAD_REQUEST, "SU003", "비밀번호가 일치하지 않습니다.");

    override val errorName: String
        get() = this.name
}