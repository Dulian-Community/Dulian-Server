package dulian.dulian.domain.board.entity

import dulian.dulian.global.config.db.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Comment("태그 정보")
@Table(
    indexes = [
        Index(name = "idx_tag_board_id", columnList = "board_id")
    ]
)
@SQLRestriction("deleted_at is null")
@SQLDelete(sql = "update tag set #deleted_at = now() WHERE tag_id = ?")
class Tag(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("태그 정보 IDX")
    val tagId: Long? = null,

    @Column(name = "name", length = 10, nullable = false, updatable = false)
    @Comment("태그명")
    val name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("게시물 정보 IDX")
    val board: Board
) : BaseEntity() {

    companion object {
        fun of(
            name: String,
            board: Board
        ) = Tag(
            name = name,
            board = board
        )
    }
}
