package dulian.dulian.domain.auth.exception

import dulian.dulian.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class AccountFindErrorCode(
    override val httpStatus: HttpStatus,
    override val errorCode: String,
    override val message: String,
) : ErrorCode {

    NOT_EXISTED_EMAIL(HttpStatus.BAD_REQUEST, "AF001", "존재하지 않는 이메일입니다."),
    INVALID_EMAIL_CODE(HttpStatus.BAD_REQUEST, "AF002", "유효하지 않은 이메일 코드입니다."),
    INVALID_EMAIL_OR_USER_ID(HttpStatus.BAD_REQUEST, "AF003", "유효하지 않은 이메일 또는 아이디입니다.");

    override val errorName: String
        get() = this.name
}
