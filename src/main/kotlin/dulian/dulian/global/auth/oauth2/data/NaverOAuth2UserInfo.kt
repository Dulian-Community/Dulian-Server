package dulian.dulian.global.auth.oauth2.data

class NaverOAuth2UserInfo(
    attributes: Map<String, Any>
) : OAuth2UserInfo(attributes) {

    override fun getId(): String {
        val response = attributes["response"] as Map<*, *>
        println(response["id"])
        val r = response["id"] as String
        println(r)
        return response["id"] as String
    }
}
