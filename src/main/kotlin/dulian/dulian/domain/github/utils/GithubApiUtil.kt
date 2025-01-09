package dulian.dulian.domain.github.utils

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class GithubApiUtil(
    private val restTemplate: RestTemplate
) {
    fun callApi(
        url: String,
        token: String,
        queryParam: Map<String, String>,
    ): ResponseEntity<String> {
        // 헤더 생성
        val headers = HttpHeaders()
        headers["Authorization"] = "Bearer $token"
        headers["X-Github-Api-Version"] = "2022-11-28"
        headers["Accept"] = "application/vnd.github+json"

        // HttpEntity에 헤더 추가
        val entity = HttpEntity<String>(headers)

        // URL과 쿼리 파라미터를 이용해 URI 생성
        val uri = UriComponentsBuilder.fromUriString(url)
            .apply {
                queryParam.forEach { (key, value) ->
                    queryParam(key, value)
                }
            }
            .build()
            .toUri()

        // GET 요청
        return restTemplate.exchange(uri, HttpMethod.GET, entity, String::class.java)
    }
}