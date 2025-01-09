package dulian.dulian.global.auth.oauth2.service

import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.global.auth.oauth2.data.CustomOAuth2User
import dulian.dulian.global.exception.CommonErrorCode
import dulian.dulian.global.exception.CustomException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.transaction.Transactional
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(
    private val memberRepository: MemberRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val log = KotlinLogging.logger {}

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val delegate: OAuth2UserService<OAuth2UserRequest, OAuth2User> = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)

        val registrationId = userRequest?.clientRegistration?.registrationId
        val usernameAttributeName = userRequest?.clientRegistration
            ?.providerDetails
            ?.userInfoEndpoint
            ?.userNameAttributeName
        val attributes = oAuth2User.attributes

        // Github 로그인만 허용
        if (registrationId?.uppercase() != "GITHUB") {
            log.error { "$registrationId 로 로그인 시도" }
            throw CustomException(CommonErrorCode.UNAUTHORIZED)
        }

        // Github Access Token 파싱
        val githubAccessToken = userRequest.accessToken?.tokenValue
            ?: throw CustomException(CommonErrorCode.UNAUTHORIZED)

        // Github ID 파싱
        val githubId = attributes["login"].toString()

        // Github Unique ID 파싱
        val githubUniqueId = attributes["id"].toString()

        // Email 파싱
        val email = attributes["email"].toString()

        // 이미 가입된 회원인지 확인 후 가입되지 않은 회원이면 저장
        val savedMember = memberRepository.findByUserId(githubUniqueId)
        val memberId = if (savedMember == null) {
            memberRepository.save(
                Member.ofOAuth2(
                    githubUniqueId,
                    githubId,
                    email,
                    githubAccessToken
                )
            ).memberId!!
        } else {
            savedMember.updateGithubAccessToken(githubAccessToken)
            savedMember.memberId!!
        }

        // MemberId를 인증 객체에 넣기 위한 변환 작업
        val mutableAttributes = attributes.toMutableMap()
        mutableAttributes[usernameAttributeName!!] = memberId.toString()

        return CustomOAuth2User(
            attributes = mutableAttributes,
            nameAttributeKey = usernameAttributeName,
            userId = memberId.toString()
        )
    }
}
