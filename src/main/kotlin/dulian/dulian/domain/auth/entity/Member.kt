package dulian.dulian.domain.auth.entity

import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.global.auth.enums.SocialType
import dulian.dulian.global.config.db.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.springframework.security.crypto.password.PasswordEncoder

@Entity
@Comment("회원 정보")
class Member(

    // TODO : 추후 Github Login 만 가능하므로 불필요한 컬럼 삭제

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
    var password: String? = null,

    @Column(name = "email", length = 50, updatable = false)
    @Comment("이메일")
    val email: String? = null,

    @Column(name = "nickname", length = 10, nullable = false)
    @Comment("닉네임")
    val nickname: String,

    @Column(name = "social_type", length = 10, updatable = false, columnDefinition = "VARCHAR(10)")
    @Enumerated(EnumType.STRING)
    @Comment("소셜 로그인 타입")
    val socialType: SocialType? = null,

    @Column(name = "github_access_token", length = 100)
    @Comment("Github 엑세스 토큰")
    var githubAccessToken: String? = null,
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
            githubUniqueId: String,
            nickname: String,
            email: String,
            githubAccessToken: String,
        ): Member = Member(
            userId = githubUniqueId,
            nickname = nickname,
            email = email,
            socialType = SocialType.GITHUB,
            githubAccessToken = githubAccessToken,
        )
    }

    fun resetPassword(
        passwordEncoder: PasswordEncoder,
        newPassword: String
    ) {
        this.password = passwordEncoder.encode(newPassword)
    }

    fun updateGithubAccessToken(
        githubAccessToken: String
    ) {
        this.githubAccessToken = githubAccessToken
    }
}
