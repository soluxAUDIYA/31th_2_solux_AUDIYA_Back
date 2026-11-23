package com.solux31th.audiya.domain.auth.service;

import com.solux31th.audiya.domain.auth.dto.response.AuthTokenResponse;
import com.solux31th.audiya.domain.auth.entity.User;
import com.solux31th.audiya.domain.auth.redis.RedisKey;
import com.solux31th.audiya.global.security.JwtProvider;
import com.solux31th.audiya.global.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

// access 토큰과 refresh 토큰 발급
// - access: JWT, 30분
// - refresh: 무작위 문자열. 원문 대신 해시를 Redis에 저장하고, 기기(deviceId)마다 하나만 둔다
@Service
@RequiredArgsConstructor
public class TokenService {

    // 로그인 유지를 켰을 때 14일, 껐을 때 1일 (가입 직후는 14일)
    private static final Duration REFRESH_TTL_KEEP = Duration.ofDays(14);
    private static final Duration REFRESH_TTL_SHORT = Duration.ofDays(1);

    private static final String FIELD_TOKEN_HASH = "tokenHash";
    private static final String FIELD_KEEP_LOGGED_IN = "keepLoggedIn";

    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redis;

    public AuthTokenResponse issueTokens(User user, String deviceId, boolean keepLoggedIn) {
        Duration refreshTtl = keepLoggedIn ? REFRESH_TTL_KEEP : REFRESH_TTL_SHORT;

        String accessToken = jwtProvider.createAccessToken(user.getId());

        // refresh 토큰 앞에 userId를 붙인다. 재발급 요청에는 userId가 없어서, 이 값으로 Redis 키를 찾기 위해서다
        String refreshToken = user.getId() + "." + CryptoUtil.randomToken();

        // 같은 기기에서 다시 로그인하면 이전 refresh 토큰은 덮어써서 무효가 된다
        String key = RedisKey.refresh(user.getId(), deviceId);
        redis.opsForHash().putAll(key, Map.of(
                FIELD_TOKEN_HASH, CryptoUtil.sha256Hex(refreshToken),
                FIELD_KEEP_LOGGED_IN, String.valueOf(keepLoggedIn)));
        redis.expire(key, refreshTtl);

        return new AuthTokenResponse(
                user.getId(),
                accessToken,
                jwtProvider.getAccessTokenTtlSeconds(),
                refreshToken,
                refreshTtl.toSeconds(),
                new AuthTokenResponse.UserSummary(user.getNickname(), user.isOnboardingCompleted()));
    }
}