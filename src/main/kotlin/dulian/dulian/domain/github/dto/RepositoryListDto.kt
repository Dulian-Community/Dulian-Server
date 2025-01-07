package dulian.dulian.domain.github.dto

class RepositoryListDto {

    data class Response(
        val result: List<Repositories>,
        val totalElements: Int,
    ) {
        companion object {
            fun of(
                responses: List<RepositoryListApiResponse>
            ) = Response(
                result = responses.groupBy { it.repositoryName.split("/").first() }
                    .map { (parentName, repositories) ->
                        Repositories.of(parentName, repositories)
                    },
                totalElements = responses.count()
            )
        }

        data class Repositories(
            val parentName: String,
            val avatarUrl: String,
            val repositories: List<Repository>
        ) {
            companion object {
                fun of(
                    parentName: String,
                    responses: List<RepositoryListApiResponse>
                ) = Repositories(
                    parentName = parentName,
                    avatarUrl = responses.first().avatarUrl,
                    repositories = responses.map {
                        Repository.of(it)
                    }
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
}
