package dulian.dulian.domain.github.dto

class RepositoryListDto {

    data class Response(
        val parentName: String,
        val repositories: List<Repository>,
        val avatarUrl: String
    ) {
        companion object {
            fun of(
                parentName: String,
                responses: List<RepositoryListApiResponse>
            ) = Response(
                parentName = parentName,
                repositories = responses.map {
                    Repository.of(it)
                },
                avatarUrl = responses.first().avatarUrl
            )
        }

        data class Repository(
            val repositoryName: String,
            val language: String,
            val visibility: String,
        ) {
            companion object {
                fun of(
                    response: RepositoryListApiResponse
                ) = with(response) {
                    Repository(
                        repositoryName = repositoryName,
                        language = language,
                        visibility = visibility
                    )
                }
            }
        }
    }
}
