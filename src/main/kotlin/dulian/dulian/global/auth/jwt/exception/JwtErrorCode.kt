package dulian.dulian.global.auth.jwt.exception

import dulian.dulian.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class JwtErrorCode(
    override val httpStatus: HttpStatus,
    override val errorCode: String,
    override val message: String,
) : ErrorCode {

    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "JWT001", "Access Token이 만료되었습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "JWT002", "유효하지 않은 Access Token입니다.");

    override val errorName: String
        get() = this.name
}

