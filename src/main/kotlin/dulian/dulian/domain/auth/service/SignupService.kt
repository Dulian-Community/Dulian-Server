package dulian.dulian.domain.auth.service

import dulian.dulian.domain.auth.repository.MemberRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class SignupService(
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun signup() {
        
    }
}
