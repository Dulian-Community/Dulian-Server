package dulian.dulian.domain.github.dto

class CommitListDto {
    data class Response(
        val result: List<Commit>,
        val totalElements: Int
    ) {
        companion object {
            fun of(
                responses: List<CommitListApiResponse>
            ) = Response(
                result = responses.map {
                    Commit.of(it)
                },
                totalElements = responses.size
            )
        }

        data class Commit(
            val message: String,
            val sha: String
        ) {
            companion object {
                fun of(
                    response: CommitListApiResponse
                ) = Commit(
                    message = response.message,
                    sha = response.sha
                )
            }
        }
    }
}
