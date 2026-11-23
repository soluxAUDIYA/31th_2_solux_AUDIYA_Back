package com.solux31th.audiya.domain.auth.service;

import com.solux31th.audiya.domain.auth.exception.AuthErrorCode;
import com.solux31th.audiya.domain.auth.redis.RedisKey;
import com.solux31th.audiya.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

// 로그인 실패 횟수 관리. 전화번호별로 5분 안에 5번 틀리면 남은 시간 동안 로그인을 막는다
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int MAX_FAILS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(5);

    // 카운트를 올리고, 만료 시간이 없으면 그때 건다 (INCR과 만료 설정을 한 번에 처리해서 TTL 없는 키가 남지 않게 한다)
    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL_SCRIPT = new DefaultRedisScript<>("""
            local count = redis.call('INCR', KEYS[1])
            if redis.call('PTTL', KEYS[1]) == -1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return count
            """, Long.class);

    private final StringRedisTemplate redis;

    // 로그인 전에 호출한다. 잠긴 상태면 비밀번호를 확인하지 않고 바로 거절한다
    public void checkNotLocked(String phone) {
        String value = redis.opsForValue().get(RedisKey.loginFail(phone));
        if (value != null && Long.parseLong(value) >= MAX_FAILS) {
            throw new BusinessException(AuthErrorCode.LOGIN_ATTEMPTS_EXCEEDED);
        }
    }

    // 실패하면 횟수를 올린다. 첫 실패 시점부터 5분 뒤에 초기화된다
    public void recordFailure(String phone) {
        redis.execute(INCREMENT_WITH_TTL_SCRIPT,
                List.of(RedisKey.loginFail(phone)),
                String.valueOf(WINDOW.toMillis()));
    }

    // 로그인에 성공하면 실패 횟수를 지운다
    public void clear(String phone) {
        redis.delete(RedisKey.loginFail(phone));
    }
}