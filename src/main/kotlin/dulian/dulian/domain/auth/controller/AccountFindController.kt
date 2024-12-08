package dulian.dulian.domain.auth.controller

import dulian.dulian.domain.auth.dto.FindIdDto
import dulian.dulian.domain.auth.dto.FindPasswordDto
import dulian.dulian.domain.auth.service.AccountFindService
import dulian.dulian.global.common.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AccountFindController(
    private val accountFindService: AccountFindService
) {

    /**
     * 아이디 찾기 - 이메일 전송 API
     */
    @PostMapping("/find-id/step1")
    fun findIdStep1(
        @RequestBody @Valid request: FindIdDto.Step1Request
    ): ResponseEntity<ApiResponse<Unit>> {
        accountFindService.findIdStep1(request)

        return ApiResponse.success()
    }

    /**
     * 아이디 찾기 - 인증 코드 검증 API
     */
    @PostMapping("/find-id/step2")
    fun findIdStep2(
        @RequestBody @Valid request: FindIdDto.Step2Request
    ): ResponseEntity<ApiResponse<FindIdDto.Response>> {
        val userId = accountFindService.findIdStep2(request)

        return ApiResponse.success(FindIdDto.Response(userId))
    }

    /**
     * 비밀번호 재설정 API
     */
    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestBody @Valid request: FindPasswordDto.Request
    ): ResponseEntity<ApiResponse<Unit>> {
        accountFindService.resetPassword(request)

        return ApiResponse.success()
    }
}
