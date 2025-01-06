package dulian.dulian.domain.review.service

import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.github.dto.RepositoryListApiResponse
import dulian.dulian.domain.github.service.GithubApiService
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.SecurityUtils
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ReviewService(
    private val githubApiService: GithubApiService,
    private val memberRepository: MemberRepository,
) {

    fun getRepositoryList(): List<RepositoryListApiResponse> {
        val member = memberRepository.findByIdOrNull(SecurityUtils.getCurrentUserId())
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // TODO : 추후 Member-GithubAccessToken NotNull로 변경
        return githubApiService.fetchRepositoryList(member.githubAccessToken!!)
    }
}
