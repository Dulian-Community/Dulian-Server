package dulian.dulian.domain.github.controller

import dulian.dulian.domain.github.dto.BranchListDto
import dulian.dulian.domain.github.dto.RepositoryListDto
import dulian.dulian.domain.github.service.GithubApiService
import dulian.dulian.global.common.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/github")
class GithubApiController(
    private val githubApiService: GithubApiService
) {

    /**
     * Github Repository 목록 조회 API
     */
    @GetMapping("/repositories")
    fun getRepositoryList(): ResponseEntity<ApiResponse<RepositoryListDto.Response>> {

        return ApiResponse.success(githubApiService.getRepositoryList())
    }

    /**
     * Github Repository Branch 목록 조회 API
     */
    @GetMapping("/branches/{owner}/{repo}")
    fun branches(
        @PathVariable("owner") owner: String,
        @PathVariable("repo") repo: String
    ): ResponseEntity<ApiResponse<BranchListDto.Response>> {
        return ApiResponse.success(githubApiService.getBranchList(owner, repo))
    }
}
