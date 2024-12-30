package dulian.dulian.global.auth.oauth2.service

import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.global.auth.enums.SocialType
import dulian.dulian.global.auth.oauth2.data.CustomOAuth2User
import dulian.dulian.global.auth.oauth2.factory.OAuth2UserInfoFactory
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

        // 소셜 로그인 타입 구분
        val socialType = SocialType.valueOf(registrationId?.uppercase()!!)

        val oAuth2UserInfo = OAuth2UserInfoFactory.of(socialType, attributes)

        // 이미 가입된 회원인지 확인 후 가입되지 않은 회원이면 저장
        val savedMember = memberRepository.findByUserIdAndSocialType(oAuth2UserInfo.getId(), socialType)
        val memberId = if (savedMember == null) {
            memberRepository.save(Member.ofOAuth2(oAuth2UserInfo, socialType)).memberId!!
        } else {
            savedMember.memberId!!
        }

        return CustomOAuth2User(
            attributes = if (socialType == SocialType.NAVER) {
                @Suppress("UNCHECKED_CAST")
                oAuth2User.attributes["response"] as Map<String, Any>
            } else {
                oAuth2User.attributes
            },
            nameAttributeKey = if (socialType == SocialType.NAVER) {
                "id"
            } else {
                usernameAttributeName!!
            },
            userId = memberId.toString()
        )
    }
}
