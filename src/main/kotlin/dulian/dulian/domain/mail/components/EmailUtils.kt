package dulian.dulian.domain.mail.components

import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.MailjetResponse
import com.mailjet.client.resource.Emailv31
import dulian.dulian.domain.mail.dto.EmailDto
import dulian.dulian.domain.mail.entity.EmailLog
import dulian.dulian.domain.mail.enums.EmailTemplateCode
import dulian.dulian.domain.mail.exception.EmailErrorCode
import dulian.dulian.domain.mail.repository.EmailLogRepository
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.ClientUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.time.LocalDateTime

@Component
class EmailUtils(
    @Value("\${spring.mail.username}")
    private val from: String,
    @Value("\${spring.mail.mailjet.api-key}")
    private val apiKey: String,
    @Value("\${spring.mail.mailjet.secret-key}")
    private val secretKey: String,
    private val templateEngine: TemplateEngine,
    private val emailLogRepository: EmailLogRepository
) {
    private val log = KotlinLogging.logger {}

    /**
     * 메일 전송
     *
     * @param emailDto 메일 정보
     */
    fun sendEmail(emailDto: EmailDto) {
        // 3분 내에 동일한 이메일로 3회 이상 요청이 들어온 경우 예외 처리
        checkTooManyRequest(emailDto.recipient)

        val options = ClientOptions.builder()
            .apiKey(apiKey)
            .apiSecretKey(secretKey)
            .build()

        val client = MailjetClient(options)
        val request = createRequest(emailDto)

        try {
            val response = client.post(request)
            handleResponse(response, emailDto)
        } catch (e: Exception) {
            log.error(e) { "메일 전송 중 오류 발생" }
            emailLogRepository.save(EmailLog.ofFailLog(emailDto.recipient, emailDto.templateCode))
            throw CustomException(EmailErrorCode.FAILED_SEND_EMAIL)
        }
    }

    /**
     * 메일 전송 요청 생성
     */
    private fun createRequest(emailDto: EmailDto) = MailjetRequest(Emailv31.resource)
        .property(
            Emailv31.MESSAGES, JSONArray()
                .put(
                    JSONObject()
                        .put(
                            Emailv31.Message.FROM, JSONObject()
                                .put("Email", from)
                                .put("Name", "두리안")
                        )
                        .put(
                            Emailv31.Message.TO, JSONArray()
                                .put(
                                    JSONObject()
                                        .put("Email", emailDto.recipient)
                                )
                        )
                        .put(Emailv31.Message.SUBJECT, emailDto.templateCode.subject)
                        .put(
                            Emailv31.Message.HTMLPART,
                            setContext(emailDto.templateCode, emailDto.variables)
                        )
                )
        )

    /**
     * 메일 템플릿에 변수 적용
     */
    private fun setContext(
        templateCode: EmailTemplateCode,
        variables: Map<String, Any>
    ): String {
        val context = Context()
        context.setVariables(variables)

        return templateEngine.process("/mail/${templateCode.templateName}", context)
    }

    /**
     * 메일 전송 결과 처리
     */
    private fun handleResponse(
        response: MailjetResponse,
        emailDto: EmailDto
    ) {
        if (response.status == 200) {
            emailLogRepository.save(EmailLog.ofSuccessLog(emailDto.recipient, emailDto.templateCode))
        } else {
            log.error { "메일 전송 실패 - ${response.status}" }
            emailLogRepository.save(EmailLog.ofFailLog(emailDto.recipient, emailDto.templateCode))
            throw CustomException(EmailErrorCode.FAILED_SEND_EMAIL)
        }
    }

    /**
     * 3분 내에 동일한 이메일로 3회 이상 요청이 들어온 경우 예외 처리
     */
    private fun checkTooManyRequest(recipient: String) {
        val savedEmailLogs = emailLogRepository.findTop3ByEmailOrAccessIp(recipient, ClientUtils.getClientIp())
        val recentEmailCount = savedEmailLogs.count {
            it.createdAt.isAfter(LocalDateTime.now().minusMinutes(3))
        }

        if (recentEmailCount >= 3) {
            throw CustomException(EmailErrorCode.TOO_MANY_REQUEST)
        }
    }

}
