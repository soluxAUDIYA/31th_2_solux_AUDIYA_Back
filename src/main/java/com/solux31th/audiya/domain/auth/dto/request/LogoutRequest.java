package com.solux31th.audiya.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

// 로그아웃 요청. 이 기기의 refresh 토큰만 삭제한다
public record LogoutRequest(
        @NotBlank(message = "DEVICE_ID_REQUIRED")
        String deviceId
) {
}
