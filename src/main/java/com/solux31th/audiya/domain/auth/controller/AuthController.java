package com.solux31th.audiya.domain.auth.controller;

import com.solux31th.audiya.domain.auth.dto.request.LoginRequest;
import com.solux31th.audiya.domain.auth.dto.request.LogoutRequest;
import com.solux31th.audiya.domain.auth.dto.request.PhoneVerificationConfirmRequest;
import com.solux31th.audiya.domain.auth.dto.request.PhoneVerificationRequest;
import com.solux31th.audiya.domain.auth.dto.request.TokenRefreshRequest;
import com.solux31th.audiya.domain.auth.dto.response.PhoneVerificationSendResponse;
import com.solux31th.audiya.domain.auth.dto.response.VerificationTokenResponse;
import com.solux31th.audiya.domain.auth.service.PhoneVerificationService;
import com.solux31th.audiya.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import com.solux31th.audiya.domain.auth.dto.request.SignupRequest;
import com.solux31th.audiya.domain.auth.dto.response.AuthTokenResponse;
import com.solux31th.audiya.domain.auth.service.AuthService;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final PhoneVerificationService phoneVerificationService;
    private final AuthService authService;

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

    // 회원가입
    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<AuthTokenResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }


    // 로그인
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    // 토큰 재발급
    @Operation(summary = "토큰 재발급")
    @PostMapping("/token/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request));
    }

    // 로그아웃 (Bearer 토큰 필요)
    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long userId,
                                    @Valid @RequestBody LogoutRequest request) {
        authService.logout(userId, request);
        return ApiResponse.ok();
    }
}