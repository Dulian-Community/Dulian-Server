package dulian.dulian.domain.github.service

import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.github.dto.BranchListApiResponse
import dulian.dulian.domain.github.dto.BranchListDto
import dulian.dulian.domain.github.dto.RepositoryListApiResponse
import dulian.dulian.domain.github.dto.RepositoryListDto
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class GithubApiService(
    private val memberRepository: MemberRepository,
    private val restTemplate: RestTemplate
) {
    private val log = KotlinLogging.logger { }

    @Transactional(readOnly = true)
    fun getRepositoryList(): RepositoryListDto.Response {
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // TODO : 추후 Member-GithubAccessToken NotNull로 변경
        val apiResult = fetchRepositoryList(member.githubAccessToken!!)
        return RepositoryListDto.Response.of(apiResult)
    }

    @Transactional(readOnly = true)
    fun getBranchList(
        owner: String,
        repo: String
    ): BranchListDto.Response {
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        val apiResult = fetchBranchList(member.githubAccessToken!!, owner, repo)
        return BranchListDto.Response.of(apiResult)
    }

    fun fetchRepositoryList(
        token: String
    ): List<RepositoryListApiResponse> {
        val url = "https://api.github.com/user/repos"

        // 헤더 설정
        val headers = HttpHeaders()
        headers["Authorization"] = "Bearer $token"
        headers["X-Github-Api-Version"] = "2022-11-28"
        headers["Accept"] = "application/vnd.github+json"

        // HttpEntity에 헤더 추가
        val entity = HttpEntity<String>(headers)

        val uri = UriComponentsBuilder.fromUriString(url)
            .queryParam("type", "all")
            .queryParam("sort", "pushed")
            .queryParam("direction", "desc")
            .queryParam("per_page", "100")
            .build()
            .toUri()

        // GET 요청을 보내고 응답을 ResponseEntity로 받음
        val response: ResponseEntity<String> =
            restTemplate.exchange(uri, HttpMethod.GET, entity, String::class.java)

        if (!response.statusCode.is2xxSuccessful) {
            log.error { "[$uri][token : $token] - API 호출 실패" }
            throw CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR)
        }

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
        val url = "https://api.github.com/repos/$owner/$repo/branches"

        // 헤더 설정
        val headers = HttpHeaders()
        headers["Authorization"] = "Bearer $token"
        headers["X-Github-Api-Version"] = "2022-11-28"
        headers["Accept"] = "application/vnd.github+json"

        // HttpEntity에 헤더 추가
        val entity = HttpEntity<String>(headers)

        val uri = UriComponentsBuilder.fromUriString(url)
            .queryParam("per_page", "100")
            .build()
            .toUri()

        // GET 요청을 보내고 응답을 ResponseEntity로 받음
        val response: ResponseEntity<String> =
            restTemplate.exchange(uri, HttpMethod.GET, entity, String::class.java)

        if (!response.statusCode.is2xxSuccessful) {
            log.error { "[$uri][token : $token] - API 호출 실패" }
            throw CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR)
        }

        val parser = JSONParser()
        val jsonArray = parser.parse(response.body) as JSONArray
        return jsonArray.map {
            val data = it as JSONObject

            BranchListApiResponse.of(data["name"].toString())
        }
    }
}
