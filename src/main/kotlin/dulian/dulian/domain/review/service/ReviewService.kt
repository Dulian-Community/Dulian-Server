package dulian.dulian.domain.review.service

import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class ReviewService(
    private val memberRepository: MemberRepository,
    private val restTemplate: RestTemplate,
) {

    fun getRepositoryList(

    ) {
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        var url = "https://api.github.com/users/${member.nickname}/repos"
//        val url = "https://api.github.com/user/repos"

        // 헤더 설정
        var headers = HttpHeaders()
        headers["Authorization"] = "Bearer ${member.githubAccessToken}"
        headers["X-Github-Api-Version"] = "2022-11-28"

        // HttpEntity에 헤더 추가
        var entity = HttpEntity<String>(headers)

        var uri = UriComponentsBuilder.fromUriString(url)
            .queryParam("type", "all")
            .queryParam("sort", "updated")
            .queryParam("direction", "desc")
            .queryParam("per_page", "100")
            .build().toUri()

        // GET 요청을 보내고 응답을 ResponseEntity로 받음
        var response: ResponseEntity<String> =
            restTemplate.exchange(uri, HttpMethod.GET, entity, String::class.java)
        var parser = JSONParser()


        val jsonArray = parser.parse(response.body) as JSONArray
        jsonArray.forEach {
            val data = it as JSONObject

            val id = data["id"] as Long
            val name = data["name"].toString()
            val fullName = data["full_name"].toString()
            val language = data["language"].toString()

            println("id: $id, name: $name, fullName: $fullName, language: $language")
        }

        println(jsonArray.count())


        url = "https://api.github.com/users/${member.nickname}/orgs"

        headers = HttpHeaders()
        headers["Authorization"] = "Bearer ${member.githubAccessToken}"
        headers["X-Github-Api-Version"] = "2022-11-28"

        // HttpEntity에 헤더 추가
        entity = HttpEntity<String>(headers)

        uri = UriComponentsBuilder.fromUriString(url)
            .queryParam("type", "all")
            .queryParam("sort", "updated")
            .queryParam("direction", "desc")
            .queryParam("per_page", "100")
            .build().toUri()

        response = restTemplate.exchange(uri, HttpMethod.GET, entity, String::class.java)
        parser = JSONParser()

        println()
    }
}