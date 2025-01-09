package dulian.dulian.domain.github.service

import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.github.components.GithubApiComponent
import dulian.dulian.domain.github.dto.BranchListDto
import dulian.dulian.domain.github.dto.CommitListDto
import dulian.dulian.domain.github.dto.RepositoryListDto
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GithubApiService(
    private val memberRepository: MemberRepository,
    private val githubApiComponent: GithubApiComponent,
) {
    @Transactional(readOnly = true)
    fun getRepositoryList(): RepositoryListDto.Response {
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // TODO : 추후 Member-GithubAccessToken NotNull로 변경
        val apiResult = githubApiComponent.fetchRepositoryList(member.githubAccessToken!!)
        return RepositoryListDto.Response.of(apiResult)
    }

    @Transactional(readOnly = true)
    fun getBranchList(
        owner: String,
        repo: String
    ): BranchListDto.Response {
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        val apiResult = githubApiComponent.fetchBranchList(member.githubAccessToken!!, owner, repo)
        return BranchListDto.Response.of(apiResult)
    }

    @Transactional(readOnly = true)
    fun getCommitList(
        owner: String,
        repo: String,
        branch: String
    ): CommitListDto.Response {
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        val apiResult = githubApiComponent.fetchCommitList(member.githubAccessToken!!, owner, repo, branch)
        return CommitListDto.Response.of(apiResult)
    }
    // TODO : RestTemplate Error Handler 추가
}
