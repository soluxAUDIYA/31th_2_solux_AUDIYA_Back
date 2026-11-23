package com.solux31th.audiya.domain.auth.service;

import com.solux31th.audiya.domain.auth.dto.response.AuthTokenResponse;
import com.solux31th.audiya.domain.auth.entity.User;
import com.solux31th.audiya.domain.auth.exception.AuthErrorCode;
import com.solux31th.audiya.domain.auth.redis.RedisKey;
import com.solux31th.audiya.global.exception.BusinessException;
import com.solux31th.audiya.global.security.JwtProvider;
import com.solux31th.audiya.global.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

// access 토큰과 refresh 토큰 발급, 재발급, 폐기
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

    // 저장된 해시와 비교해서 같을 때만 새 해시로 바꾸고 만료 시간을 다시 건다 (확인과 교체를 한 번에 처리한다)
    // 해시가 다르면 0
    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>("""
            local stored = redis.call('HGET', KEYS[1], 'tokenHash')
            if not stored then
                return 0
            end
            if stored ~= ARGV[1] then
                return 0
            end
            redis.call('HSET', KEYS[1], 'tokenHash', ARGV[2])
            redis.call('PEXPIRE', KEYS[1], ARGV[3])
            return 1
            """, Long.class);

    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redis;

    // 새 토큰 발급 (가입, 로그인)
    public AuthTokenResponse issueTokens(User user, String deviceId, boolean keepLoggedIn) {
        Duration refreshTtl = keepLoggedIn ? REFRESH_TTL_KEEP : REFRESH_TTL_SHORT;
        String refreshToken = newRefreshToken(user.getId());

        // 같은 기기에서 다시 로그인하면 이전 refresh 토큰은 덮어써서 무효가 된다
        String key = RedisKey.refresh(user.getId(), deviceId);
        redis.opsForHash().putAll(key, Map.of(
                FIELD_TOKEN_HASH, CryptoUtil.sha256Hex(refreshToken),
                FIELD_KEEP_LOGGED_IN, String.valueOf(keepLoggedIn)));
        redis.expire(key, refreshTtl);

        return toResponse(user, refreshToken, refreshTtl);
    }

    // refresh 토큰 앞부분의 userId를 꺼낸다. 형식이 맞지 않으면 유효하지 않은 토큰으로 처리한다
    public Long extractUserId(String refreshToken) {
        int dot = refreshToken.indexOf('.');
        if (dot <= 0) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
        try {
            return Long.parseLong(refreshToken.substring(0, dot));
        } catch (NumberFormatException e) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
    }

    // 토큰 재발급. 기존 refresh 토큰은 새 토큰으로 교체되어 한 번만 쓸 수 있다 (rotation)
    public AuthTokenResponse rotate(User user, String deviceId, String oldRefreshToken) {
        String key = RedisKey.refresh(user.getId(), deviceId);

        // 처음 로그인할 때 정한 로그인 유지 여부를 그대로 따른다
        Object keep = redis.opsForHash().get(key, FIELD_KEEP_LOGGED_IN);
        if (keep == null) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }
        Duration refreshTtl = Boolean.parseBoolean(keep.toString()) ? REFRESH_TTL_KEEP : REFRESH_TTL_SHORT;

        String newRefreshToken = newRefreshToken(user.getId());
        Long result = redis.execute(ROTATE_SCRIPT,
                List.of(key),
                CryptoUtil.sha256Hex(oldRefreshToken),
                CryptoUtil.sha256Hex(newRefreshToken),
                String.valueOf(refreshTtl.toMillis()));
        if (result == null || result != 1L) {
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        return toResponse(user, newRefreshToken, refreshTtl);
    }

    // 해당 기기의 refresh 토큰 삭제 (이미 없어도 성공)
    public void revoke(Long userId, String deviceId) {
        redis.delete(RedisKey.refresh(userId, deviceId));
    }

    // refresh 토큰 앞에 userId를 붙인다. 재발급 요청에는 userId가 없어서, 이 값으로 Redis 키를 찾기 위해서다
    private String newRefreshToken(Long userId) {
        return userId + "." + CryptoUtil.randomToken();
    }

    private AuthTokenResponse toResponse(User user, String refreshToken, Duration refreshTtl) {
        return new AuthTokenResponse(
                user.getId(),
                jwtProvider.createAccessToken(user.getId()),
                jwtProvider.getAccessTokenTtlSeconds(),
                refreshToken,
                refreshTtl.toSeconds(),
                new AuthTokenResponse.UserSummary(user.getNickname(), user.isOnboardingCompleted()));
    }
}