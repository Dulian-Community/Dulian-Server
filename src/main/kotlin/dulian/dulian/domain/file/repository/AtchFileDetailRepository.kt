package dulian.dulian.domain.file.repository

import dulian.dulian.domain.file.entity.AtchFileDetail
import org.springframework.data.jpa.repository.JpaRepository

interface AtchFileDetailRepository : JpaRepository<AtchFileDetail, Long> {
}
