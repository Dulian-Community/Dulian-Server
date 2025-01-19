package dulian.dulian.domain.github.components

import dulian.dulian.domain.github.dto.BranchListApiResponse
import dulian.dulian.domain.github.dto.CommitListApiResponse
import dulian.dulian.domain.github.dto.RepositoryListApiResponse
import dulian.dulian.domain.github.utils.GithubApiUtil
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.stereotype.Component

@Component
class GithubApiComponent(
    private val githubApiUtil: GithubApiUtil
) {
    fun fetchRepositoryList(
        token: String
    ): List<RepositoryListApiResponse> {
        val response = githubApiUtil.callApi(
            url = "https://api.github.com/user/repos",
            token = token,
            queryParam = mapOf(
                "type" to "all",
                "sort" to "pushed",
                "direction" to "desc",
                "per_page" to "100"
            )
        )

        val parser = JSONParser()
        val jsonArray = parser.parse(response.body) as JSONArray
        return jsonArray.map {
            val data = it as JSONObject

            val owner = data["owner"] as JSONObject
            val avatarUrl = owner["avatar_url"].toString()
            val fullName = data["full_name"].toString()
            val language = data["language"].toString()
            val visibility = data["visibility"].toString()

            RepositoryListApiResponse(
                repositoryName = fullName,
                language = language,
                visibility = visibility,
                avatarUrl = avatarUrl
            )
        }
    }

    fun fetchBranchList(
        token: String,
        owner: String,
        repo: String
    ): List<BranchListApiResponse> {
        val response = githubApiUtil.callApi(
            url = "https://api.github.com/repos/$owner/$repo/branches",
            token = token,
            queryParam = mapOf(
                "per_page" to "100"
            )
        )

        val parser = JSONParser()
        val jsonArray = parser.parse(response.body) as JSONArray
        return jsonArray.map {
            val data = it as JSONObject

            BranchListApiResponse.of(data["name"].toString())
        }
    }

    fun fetchCommitList(
        token: String,
        owner: String,
        repo: String,
        branch: String
    ): List<CommitListApiResponse> {
        val response = githubApiUtil.callApi(
            url = "https://api.github.com/repos/$owner/$repo/commits",
            token = token,
            queryParam = mapOf(
                "sha" to branch,
                "author" to owner,
                "commiter" to owner
            )
        )

        val parser = JSONParser()
        val jsonArray = parser.parse(response.body) as JSONArray
        return jsonArray.map {
            val data = it as JSONObject

            val commit = data["commit"] as JSONObject
            val tree = commit["tree"] as JSONObject

            CommitListApiResponse.of(
                message = commit["message"].toString(),
                sha = tree["sha"].toString()
            )
        }
    }
}
