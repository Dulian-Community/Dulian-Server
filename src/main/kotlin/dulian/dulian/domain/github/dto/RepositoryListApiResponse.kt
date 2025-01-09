package dulian.dulian.domain.github.dto

data class RepositoryListApiResponse(
    val repositoryName: String,
    val language: String,
    val visibility: String,
    val avatarUrl: String
) {
}
