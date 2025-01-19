package dulian.dulian.domain.github.dto

data class CommitListApiResponse(
    val message: String,
    val sha: String
) {
    companion object {
        fun of(
            message: String,
            sha: String
        ) = CommitListApiResponse(
            message = message,
            sha = sha
        )
    }
}
