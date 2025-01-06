package dulian.dulian.domain.github.controller

import dulian.dulian.domain.github.dto.RepositoryListDto
import dulian.dulian.domain.github.service.GithubApiService
import dulian.dulian.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/github")
class GithubApiController(
    private val githubApiService: GithubApiService
) {

    @GetMapping("/repositories")
    fun getRepositoryList(): ResponseEntity<ApiResponse<List<RepositoryListDto.Response>>> {

        return ApiResponse.success(githubApiService.getRepositoryList())
    }
}
