package dulian.dulian.domain.mail.enums

enum class EmailTemplateCode(
    val subject: String,
    val templateName: String
) {
    SIGNUP_CONFIRM("[두리안] 회원가입 인증 코드", "signup-confirm"),
    FIND_ID("[두리안] 아이디 찾기 인증 코드", "find-id"),
}