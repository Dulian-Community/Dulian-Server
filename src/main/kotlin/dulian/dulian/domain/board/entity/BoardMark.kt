package dulian.dulian.domain.board.entity

import dulian.dulian.domain.auth.entity.Member
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "UK_BOARD_LIKE",
            columnNames = ["board_id", "member_id"]
        )
    ]
)
@Comment("게시물 북마크 정보")
class BoardMark(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val boardMarkId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "board_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("게시물 ID")
    val board: Board,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    @Comment("회원 ID")
    val member: Member
) {
    companion object {
        fun of(
            board: Board,
            member: Member
        ) = BoardMark(
            board = board,
            member = member
        )
    }
}