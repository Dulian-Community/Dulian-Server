package dulian.dulian.domain.github.dto

data class CommitListApiResponse(
    val message: String
) {
    companion object {
        fun of(
            message: String
        ) = CommitListApiResponse(
            message = message
        )
    }
}