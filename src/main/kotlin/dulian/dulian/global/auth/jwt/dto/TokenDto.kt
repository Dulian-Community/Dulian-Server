package dulian.dulian.global.auth.jwt.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

data class TokenDto(
    val accessToken: Token,
    val refreshToken: Token
) {

    data class Token(
        val token: String,
        val expiresIn: Long
    ) {

        /**
         * 만료까지 남은 시간을 초 단위로 반환
         */
        @get:JsonIgnore
        val expiresInSecond: Int
            get() = ((expiresIn - System.currentTimeMillis()) / 1000).toInt()

        companion object {
            fun of(
                token: String,
                expiresIn: Date
            ) = Token(
                token = token,
                expiresIn = expiresIn.time
            )
        }
    }
}
