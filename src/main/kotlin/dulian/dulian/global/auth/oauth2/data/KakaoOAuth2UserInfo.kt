package dulian.dulian.global.auth.oauth2.data

class KakaoOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {

    override fun getId(): String {
        return attributes["id"].toString()
    }
}
