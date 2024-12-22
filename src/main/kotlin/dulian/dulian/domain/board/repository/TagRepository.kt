package dulian.dulian.domain.board.repository

import dulian.dulian.domain.board.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository

interface TagRepository : JpaRepository<Tag, Long> {
}
