package dulian.dulian.global.auth.oauth2.data

import dulian.dulian.global.auth.enums.Role
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

class CustomOAuth2User(
    attributes: Map<String, Any>,
    nameAttributeKey: String,
    val userId: String
) : DefaultOAuth2User(
    listOf(SimpleGrantedAuthority(Role.ROLE_USER.name)),
    attributes,
    nameAttributeKey
) {
    val role: Role = Role.ROLE_USER
}
