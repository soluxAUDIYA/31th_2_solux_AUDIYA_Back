package com.solux31th.audiya.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @NotBlank(message = "PHONE_NOT_VERIFIED")
        String verificationToken,

        @NotBlank(message = "NAME_REQUIRED")
        @Pattern(regexp = "^[가-힣a-zA-Z ]{1,10}$", message = "NAME_INVALID")
        String name,

        @NotBlank(message = "NICKNAME_INVALID")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9@_^.]{2,12}$", message = "NICKNAME_INVALID")
        String nickname,

        // 영문, 숫자, 특수문자를 모두 포함한 8~20자, 공백 불가
        @NotBlank(message = "PASSWORD_INVALID")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[^A-Za-z0-9\\s])\\S{8,20}$", message = "PASSWORD_INVALID")
        String password,

        @NotBlank(message = "PASSWORD_MISMATCH")
        String passwordConfirm,

        // refresh 토큰을 기기별로 관리하는 키
        @NotBlank(message = "DEVICE_ID_REQUIRED")
        String deviceId
) {
}