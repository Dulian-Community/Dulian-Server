package dulian.dulian.domain.file.exception

import dulian.dulian.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class FileErrorCode(
    override val httpStatus: HttpStatus,
    override val errorCode: String,
    override val message: String,
) : ErrorCode {

    INVALID_FILE(HttpStatus.BAD_REQUEST, "FI001", "유효하지 않은 파일입니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "FI002", "유효하지 않은 파일 확장자입니다.");

    override val errorName: String
        get() = this.name
}
