package dulian.dulian.global.exception

class CustomException(
    val errorCode: ErrorCode
) : RuntimeException()
