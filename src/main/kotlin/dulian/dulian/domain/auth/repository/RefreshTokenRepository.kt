package dulian.dulian.domain.auth.repository

import dulian.dulian.domain.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
}
