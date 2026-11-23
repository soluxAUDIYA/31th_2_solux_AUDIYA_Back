package com.solux31th.audiya.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 로그인 요청. 전화번호(하이픈 없이 숫자만)와 비밀번호로 로그인한다
public record LoginRequest(
        @NotBlank(message = "PHONE_REQUIRED")
        String phoneNumber,

        @NotBlank(message = "PASSWORD_REQUIRED")
        String password,

        @NotNull(message = "KEEP_LOGGED_IN_REQUIRED")
        Boolean keepLoggedIn,

        @NotBlank(message = "DEVICE_ID_REQUIRED")
        String deviceId
) {
}