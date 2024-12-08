package dulian.dulian.domain.auth.exception

import dulian.dulian.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class LoginErrorCode(
    override val httpStatus: HttpStatus,
    override val errorCode: String,
    override val message: String,
) : ErrorCode {

    FAILED_TO_LOGIN(HttpStatus.BAD_REQUEST, "LI001", "아이디 혹은 비밀번호가 일치하지 않습니다."), ;

    override val errorName: String
        get() = this.name
}

