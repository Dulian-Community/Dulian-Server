package dulian.dulian.domain.auth.controller

import dulian.dulian.domain.auth.dto.SignupDto
import dulian.dulian.domain.auth.service.SignupService
import dulian.dulian.global.common.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val signupService: SignupService
) {

    /**
     * 회원가입 API
     */
    @PostMapping("/signup")
    fun signup(
        @RequestBody @Valid request: SignupDto.Request
    ): ResponseEntity<ApiResponse<Unit>> {
        signupService.signup(request)

        return ApiResponse.success()
    }
}
