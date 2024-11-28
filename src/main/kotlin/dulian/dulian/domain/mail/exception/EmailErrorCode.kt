package dulian.dulian.domain.mail.exception

import dulian.dulian.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class EmailErrorCode(
    override val httpStatus: HttpStatus,
    override val errorCode: String,
    override val message: String,
) : ErrorCode {

    FAILED_SEND_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, "EM001", "이메일 전송에 실패했습니다."),
    TOO_MANY_REQUEST(HttpStatus.TOO_MANY_REQUESTS, "EM002", "이메일 전송 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");

    override val errorName: String
        get() = this.name
}
