package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.dto.FindIdDto
import dulian.dulian.domain.auth.exception.AccountFindErrorCode
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.mail.components.EmailUtils
import dulian.dulian.domain.mail.dto.EmailDto
import dulian.dulian.domain.mail.entity.EmailCode
import dulian.dulian.domain.mail.repository.EmailCodeRepository
import dulian.dulian.global.exception.CustomException
import org.springframework.stereotype.Service

@Service
class AccountFindService(
    private val memberRepository: MemberRepository,
    private val emailUtils: EmailUtils,
    private val emailCodeRepository: EmailCodeRepository
) {

    fun findIdStep1(
        request: FindIdDto.Step1Request
    ) {
        // 이메일 존재 여부 검증
        require(!memberRepository.existsByEmail(request.email)) {
            throw CustomException(AccountFindErrorCode.NOT_EXISTED_EMAIL)
        }

        // 이메일 전송
        val emailDto = EmailDto.ofSignupConfirm(request.email)
        emailUtils.sendEmail(emailDto)

        // 인증 코드 저장
        emailCodeRepository.save(EmailCode.ofSignupConfirm(emailDto.getCode(), request.email))
    }
}