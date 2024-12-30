package dulian.dulian.domain.board.entity

import dulian.dulian.domain.auth.entity.Member
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Comment("게시물 좋아요 정보")
class BoardLike(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val boardLikeId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "board_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT),
        unique = true
    )
    @Comment("게시물 ID")
    val board: Board,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        nullable = false,
        updatable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT),
        unique = true
    )
    @Comment("회원 ID")
    val member: Member
) {
    companion object {
        fun of(
            board: Board,
            member: Member
        ) = BoardLike(
            board = board,
            member = member
        )
    }
}