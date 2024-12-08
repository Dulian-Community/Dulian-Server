package dulian.dulian.domain.auth.exception

import dulian.dulian.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class RefreshTokenErrorCode(
    override val httpStatus: HttpStatus,
    override val errorCode: String,
    override val message: String,
) : ErrorCode {

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A0001", "Refresh Token이 존재하지 않습니다.");

    override val errorName: String
        get() = this.name
}

