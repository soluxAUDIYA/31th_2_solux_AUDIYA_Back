package com.solux31th.audiya.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

// 토큰 재발급 요청
public record TokenRefreshRequest(
        @NotBlank(message = "REFRESH_TOKEN_REQUIRED")
        String refreshToken,

        @NotBlank(message = "DEVICE_ID_REQUIRED")
        String deviceId
) {
}