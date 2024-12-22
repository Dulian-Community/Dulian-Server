package dulian.dulian.domain.board.entity

import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.board.dto.GeneralBoardAddDto
import dulian.dulian.domain.board.enums.BoardType
import dulian.dulian.domain.file.entity.AtchFile
import dulian.dulian.global.config.db.entity.BaseEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(
    indexes = [
        Index(name = "idx_board_member_id", columnList = "member_id"),
        Index(name = "idx_board_atch_file_id", columnList = "atch_file_id")
    ]
)
@Comment("게시물 정보")
class Board(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", nullable = false, updatable = false)
    @Comment("게시물 정보 IDX")
    val boardId: Long? = null,

    @Column(name = "title", length = 100, nullable = false)
    @Comment("제목")
    val title: String,

    @Column(name = "content", length = 1000, nullable = false)
    @Comment("내용")
    val content: String,

    @Column(name = "board_type", nullable = false, updatable = false, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    @Comment("게시판 타입")
    val boardType: BoardType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("회원 정보 IDX")
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atch_file_id", nullable = true, foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @Comment("첨부파일 정보 IDX")
    val atchFile: AtchFile? = null
) : BaseEntity() {

    companion object {
        fun of(
            request: GeneralBoardAddDto.Request,
            member: Member,
            atchFile: AtchFile?
        ) = Board(
            title = request.title,
            content = request.content,
            boardType = BoardType.GENERAL,
            member = member,
            atchFile = atchFile
        )
    }
}
