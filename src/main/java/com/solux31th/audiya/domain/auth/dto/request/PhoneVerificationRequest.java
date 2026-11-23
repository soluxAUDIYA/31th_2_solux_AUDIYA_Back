package com.solux31th.audiya.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneVerificationRequest(
        @NotBlank(message = "PHONE_INVALID_FORMAT")
        @Pattern(regexp = "^01[016789][0-9]{7,8}$", message = "PHONE_INVALID_FORMAT")
        String phoneNumber
) {
}