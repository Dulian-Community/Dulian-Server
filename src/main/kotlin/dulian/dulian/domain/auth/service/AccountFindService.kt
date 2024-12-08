package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.dto.FindIdDto
import dulian.dulian.domain.auth.dto.FindPasswordDto
import dulian.dulian.domain.auth.exception.AccountFindErrorCode
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.mail.components.EmailUtils
import dulian.dulian.domain.mail.dto.EmailDto
import dulian.dulian.domain.mail.entity.EmailCode
import dulian.dulian.domain.mail.enums.EmailTemplateCode
import dulian.dulian.domain.mail.repository.EmailCodeRepository
import dulian.dulian.global.exception.CustomException
import dulian.dulian.global.utils.RandomUtils
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AccountFindService(
    private val memberRepository: MemberRepository,
    private val emailCodeRepository: EmailCodeRepository,
    private val emailUtils: EmailUtils,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun findIdStep1(
        request: FindIdDto.Step1Request
    ) {
        // 이메일 존재 여부 검증
        require(memberRepository.existsByEmail(request.email)) {
            throw CustomException(AccountFindErrorCode.NOT_EXISTED_EMAIL)
        }

        // 이메일 전송
        val emailDto = EmailDto.ofFindId(request.email)
        emailUtils.sendEmail(emailDto)

        // 인증 코드 저장
        emailCodeRepository.save(EmailCode.ofFindId(emailDto.getCode(), request.email))
    }

    @Transactional(readOnly = true)
    fun findIdStep2(
        request: FindIdDto.Step2Request
    ): String {
        // 이메일 코드 검증
        val savedEmailCode = emailCodeRepository.findByCodeAndEmailAndEmailTemplateCode(
            request.code,
            request.email,
            EmailTemplateCode.FIND_ID
        ) ?: throw CustomException(AccountFindErrorCode.INVALID_EMAIL_CODE)

        require(!savedEmailCode.createdAt.isBefore(LocalDateTime.now().minusMinutes(3))) {
            throw CustomException(AccountFindErrorCode.INVALID_EMAIL_CODE)
        }

        // 계정 조회
        return memberRepository.findByEmail(request.email)?.userId
            ?: throw CustomException(AccountFindErrorCode.NOT_EXISTED_EMAIL)
    }

    @Transactional
    fun resetPassword(
        request: FindPasswordDto.Request
    ) {
        // 계정 조회
        val member = memberRepository.findByEmailAndUserId(request.email, request.userId)
            ?: throw CustomException(AccountFindErrorCode.INVALID_EMAIL_OR_USER_ID)

        // 비밀번호 초기화
        val newPassword = RandomUtils.generateRandomString(15)
        member.resetPassword(
            passwordEncoder = passwordEncoder,
            newPassword = newPassword
        )

        // 이메일 전송
        val emailDto = EmailDto.ofResetPassword(request.email, newPassword)
        emailUtils.sendEmail(emailDto)
    }
}
