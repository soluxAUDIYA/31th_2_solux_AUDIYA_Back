package com.solux31th.audiya.domain.auth.dto.response;

public record PhoneVerificationSendResponse(int expiresInSeconds, int resendAvailableInSeconds) {
}