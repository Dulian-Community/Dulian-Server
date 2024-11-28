package dulian.dulian.domain.auth.repository

import dulian.dulian.domain.auth.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {

    fun existsByUserId(userId: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun existsByEmail(email: String): Boolean
}
