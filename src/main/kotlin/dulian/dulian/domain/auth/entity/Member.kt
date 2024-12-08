package dulian.dulian.domain.auth.entity

import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.global.auth.enums.SocialType
import dulian.dulian.global.auth.oauth2.data.OAuth2UserInfo
import dulian.dulian.global.config.db.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.util.*

@Entity
@Comment("회원 정보")
class Member(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false, updatable = false)
    @Comment("회원 정보 IDX")
    val memberId: Long? = null,

    @Column(name = "user_id", length = 100, nullable = false, updatable = false, unique = true)
    @Comment("아이디")
    val userId: String,

    @Column(name = "password", length = 100)
    @Comment("비밀번호")
    val password: String? = null,

    @Column(name = "email", length = 50, updatable = false)
    @Comment("이메일")
    val email: String? = null,

    @Column(name = "nickname", length = 10, nullable = false)
    @Comment("닉네임")
    val nickname: String,

    @Column(name = "social_type", length = 10, updatable = false, columnDefinition = "VARCHAR(10)")
    @Enumerated(EnumType.STRING)
    @Comment("소셜 로그인 타입")
    val socialType: SocialType? = null
) : BaseEntity() {

    companion object {

        fun of(request: SignupDto.Request): Member =
            Member(
                userId = request.userId,
                password = request.password,
                email = request.email,
                nickname = request.nickname
            )

        fun ofOAuth2(
            oAuth2UserInfo: OAuth2UserInfo,
            socialType: SocialType
        ): Member = Member(
            userId = oAuth2UserInfo.getId(),
            nickname = "user${UUID.randomUUID().toString().replace("-", "").substring(26)}",
            socialType = socialType
        )
    }
}
