package dulian.dulian.domain.auth.repository

import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.global.auth.enums.SocialType
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {

    fun existsByUserId(userId: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun findByUserId(userId: String): Member?

    fun existsByUserIdAndSocialType(userId: String, socialType: SocialType): Boolean

    fun findByEmail(email: String): Member?

    fun findByEmailAndUserId(email: String, userId: String): Member?
}
