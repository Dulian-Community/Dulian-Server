package dulian.dulian.global.auth.oauth2.factory

import dulian.dulian.global.auth.enums.SocialType
import dulian.dulian.global.auth.oauth2.data.GoogleOAuth2UserInfo
import dulian.dulian.global.auth.oauth2.data.KakaoOAuth2UserInfo
import dulian.dulian.global.auth.oauth2.data.NaverOAuth2UserInfo
import dulian.dulian.global.auth.oauth2.data.OAuth2UserInfo

object OAuth2UserInfoFactory {

    fun of(
        socialType: SocialType,
        attributes: Map<String, Any>
    ): OAuth2UserInfo {
        return when (socialType) {
            SocialType.KAKAO -> KakaoOAuth2UserInfo(attributes)
            SocialType.GOOGLE -> GoogleOAuth2UserInfo(attributes)
            SocialType.NAVER -> NaverOAuth2UserInfo(attributes)
        }
    }
}
