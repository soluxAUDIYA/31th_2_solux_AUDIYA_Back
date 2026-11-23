package com.solux31th.audiya.domain.auth.dto.response;

public record VerificationTokenResponse(String verificationToken, int expiresInSeconds) {
}