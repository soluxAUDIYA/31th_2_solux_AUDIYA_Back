package com.solux31th.audiya.domain.auth.dto.response;

public record AuthTokenResponse(
        Long userId,
        String accessToken,
        long accessTokenExpiresInSeconds,
        String refreshToken,
        long refreshTokenExpiresInSeconds,
        UserSummary user
) {
    public record UserSummary(String nickname, boolean onboardingCompleted) {
    }
}