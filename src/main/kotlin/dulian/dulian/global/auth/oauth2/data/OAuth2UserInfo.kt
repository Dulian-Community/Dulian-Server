package dulian.dulian.global.auth.oauth2.data

abstract class OAuth2UserInfo(
    protected val attributes: Map<String, Any>
) {
    abstract fun getId(): String
}
