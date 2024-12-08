package dulian.dulian.domain.auth.repository

import dulian.dulian.domain.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    fun findFirstByTokenAndExpiredInAfterOrderByExpiredInDesc(token: String, currentTime: LocalDateTime): RefreshToken?
}
