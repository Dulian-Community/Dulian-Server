package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.domain.auth.entity.Member
import dulian.dulian.domain.auth.exception.SignupErrorCode
import dulian.dulian.domain.auth.repository.MemberRepository
import dulian.dulian.global.exception.CustomException
import jakarta.transaction.Transactional
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class SignupService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun signup(request: SignupDto.Request) {
        // 회원가입 전 체크
        checkBeforeSignup(request)

        // 회원 저장
        request.encryptPassword(passwordEncoder)
        val member = Member.of(request)
        memberRepository.save(member)
    }

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

        // TODO : 이메일 인증 확인

    }
}
