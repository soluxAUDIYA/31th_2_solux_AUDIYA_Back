package com.solux31th.audiya.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneVerificationConfirmRequest(
        @NotBlank(message = "PHONE_INVALID_FORMAT")
        @Pattern(regexp = "^01[016789][0-9]{7,8}$", message = "PHONE_INVALID_FORMAT")
        String phoneNumber,

        @NotBlank(message = "CODE_REQUIRED")
        @Pattern(regexp = "^[0-9]{6}$", message = "CODE_FORMAT_INVALID")
        String code
) {
}