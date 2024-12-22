package dulian.dulian.domain.file.repository

import dulian.dulian.domain.file.entity.AtchFile
import org.springframework.data.jpa.repository.JpaRepository

interface AtchFileRepository : JpaRepository<AtchFile, Long> {
}
