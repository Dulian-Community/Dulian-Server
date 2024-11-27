package dulian.dulian.domain.auth.entity

import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.global.config.db.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Comment("회원 정보")
class Member(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false, updatable = false)
    @Comment("회원 정보 IDX")
    val memberId: Long? = null,

    @Column(name = "user_id", length = 12, nullable = false, updatable = false, unique = true)
    @Comment("아이디")
    val userId: String,

    @Column(name = "password", length = 100, nullable = false)
    @Comment("비밀번호")
    val password: String,

    @Column(name = "email", length = 50, nullable = false, updatable = false)
    @Comment("이메일")
    val email: String,

    @Column(name = "nickname", length = 10, nullable = false)
    @Comment("닉네임")
    val nickname: String
) : BaseEntity() {

    companion object {

        fun of(request: SignupDto.Request): Member =
            Member(
                userId = request.userId,
                password = request.password,
                email = request.email,
                nickname = request.nickname
            )
    }
}
