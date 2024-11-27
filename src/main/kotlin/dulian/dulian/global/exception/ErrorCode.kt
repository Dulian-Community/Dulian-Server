package dulian.dulian.global.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
    val httpStatus: HttpStatus
    val errorCode: String
    val message: String
    val errorName: String
}