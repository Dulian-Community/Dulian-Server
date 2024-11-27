package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.dto.SignupConfirmDto
import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.exception.SignupErrorCode
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.domain.mail.components.EmailUtils
import dulian.dulian.domain.mail.dto.EmailDto
import dulian.dulian.domain.mail.entity.EmailCode
import dulian.dulian.domain.mail.repository.EmailCodeRepository
import dulian.dulian.global.exception.CustomException
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SignupService(
    private val memberRepository: MemberRepository,
    private val emailCodeRepository: EmailCodeRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailUtils: EmailUtils
) {

    @Transactional
    fun signup(
        request: SignupDto.Request
    ) {
        // 회원가입 전 체크
        checkBeforeSignup(request)

        // 회원 저장
        request.encryptPassword(passwordEncoder)
        val member = Member.of(request)
        memberRepository.save(member)
    }

    @Transactional
    fun sendEmailConfirmCode(
        request: SignupConfirmDto.Request
    ) {
        // 이메일 중복체크
        require(!memberRepository.existsByEmail(request.email)) {
            throw CustomException(SignupErrorCode.EXISTED_EMAIL)
        }

        // 이메일 전송
        val emailDto = EmailDto.ofSignupConfirm(request.email)
        emailUtils.sendEmail(emailDto)

        // 인증 코드 저장
        emailCodeRepository.save(EmailCode.ofSignupConfirm(emailDto.getCode(), request.email))
    }

    /**
     * 회원가입 전 체크
     */
    private fun checkBeforeSignup(request: SignupDto.Request) {
        // 아이디 중복체크
        require(!memberRepository.existsByUserId(request.userId)) {
            throw CustomException(SignupErrorCode.EXISTED_USER_ID)
        }

        // 닉네임 중복체크
        require(!memberRepository.existsByNickname(request.nickname)) {
            throw CustomException(SignupErrorCode.EXISTED_NICKNAME)
        }

        // 비밀번호 확인
        require(request.checkPassword()) {
            throw CustomException(SignupErrorCode.PASSWORD_CONFIRM_FAIL)
        }

        // 이메일 인증 확인
        val savedEmailCode = emailCodeRepository.findByCodeAndEmail(request.emailConfirmCode, request.email)
            ?: throw CustomException(SignupErrorCode.INVALID_EMAIL_CODE)

        require(!savedEmailCode.createdAt.isBefore(LocalDateTime.now().minusMinutes(3))) {
            throw CustomException(SignupErrorCode.INVALID_EMAIL_CODE)
        }
    }
}
