package dulian.dulian.domain.board.exception

import dulian.dulian.global.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class BoardErrorCode(
    override val httpStatus: HttpStatus,
    override val errorCode: String,
    override val message: String,
) : ErrorCode {

    TOO_MANY_9_IMAGES(HttpStatus.BAD_REQUEST, "B001", "이미지는 최대 9개까지 등록할 수 있습니다."),
    TOO_LONG_TAG(HttpStatus.BAD_REQUEST, "B002", "태그는 10자 이내로 입력해주세요."),
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "B003", "게시물을 찾을 수 없습니다.");
    
    override val errorName: String
        get() = this.name
}
