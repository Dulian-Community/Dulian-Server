package dulian.dulian.domain.auth.entity

import dulian.dulian.global.auth.jwt.dto.TokenDto
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Entity
@Comment("Refresh Token 정보")
class RefreshToken(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Refresh Token 정보 IDX")
    val refreshTokenId: Long? = null,

    @Column(name = "token", length = 200, nullable = false, updatable = false)
    @Comment("Refresh Token")
    val token: String,

    @Column(name = "expired_in", nullable = false, updatable = false, columnDefinition = "DATETIME")
    @Comment("만료일자")
    val expiredIn: LocalDateTime,

    @Column(name = "user_id", length = 100, nullable = false, updatable = false)
    @Comment("아이디")
    val userId: String
) {

    companion object {

        fun of(
            token: TokenDto.Token,
            userId: String
        ) = RefreshToken(
            token = token.token,
            expiredIn = LocalDateTime.ofInstant(Instant.ofEpochMilli(token.expiresIn), ZoneId.systemDefault()),
            userId = userId
        )
    }
}
