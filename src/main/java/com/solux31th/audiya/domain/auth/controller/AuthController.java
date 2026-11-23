package com.solux31th.audiya.domain.auth.controller;

import com.solux31th.audiya.domain.auth.dto.request.PhoneVerificationConfirmRequest;
import com.solux31th.audiya.domain.auth.dto.request.PhoneVerificationRequest;
import com.solux31th.audiya.domain.auth.dto.response.PhoneVerificationSendResponse;
import com.solux31th.audiya.domain.auth.dto.response.VerificationTokenResponse;
import com.solux31th.audiya.domain.auth.service.PhoneVerificationService;
import com.solux31th.audiya.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PhoneVerificationService phoneVerificationService;

    // 가입 인증번호 발송
    @Operation(summary = "가입 인증번호 발송", description = "재전송도 같은 API를 호출")
    @PostMapping("/signup/phone-verifications")
    public ApiResponse<PhoneVerificationSendResponse> sendSignupCode(
            @Valid @RequestBody PhoneVerificationRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.ok(phoneVerificationService.sendSignupCode(request, httpRequest.getRemoteAddr()));
    }

    // 가입 인증번호 확인
    @Operation(summary = "가입 인증번호 확인", description = "전송된 인증번호를 확인")
    @PostMapping("/signup/phone-verifications/confirm")
    public ApiResponse<VerificationTokenResponse> confirmSignupCode(
            @Valid @RequestBody PhoneVerificationConfirmRequest request) {
        return ApiResponse.ok(phoneVerificationService.confirmSignupCode(request));
    }
}