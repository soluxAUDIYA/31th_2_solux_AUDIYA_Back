package com.solux31th.audiya.domain.auth.service;

import com.solux31th.audiya.domain.auth.dto.request.LoginRequest;
import com.solux31th.audiya.domain.auth.dto.request.LogoutRequest;
import com.solux31th.audiya.domain.auth.dto.request.SignupRequest;
import com.solux31th.audiya.domain.auth.dto.request.TokenRefreshRequest;
import com.solux31th.audiya.domain.auth.dto.response.AuthTokenResponse;
import com.solux31th.audiya.domain.auth.entity.User;
import com.solux31th.audiya.domain.auth.exception.AuthErrorCode;
import com.solux31th.audiya.domain.auth.redis.RedisKey;
import com.solux31th.audiya.domain.auth.repository.UserRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import com.solux31th.audiya.global.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redis;
    private final TokenService tokenService;
    private final LoginAttemptService loginAttemptService;

    // 회원가입
    @Transactional
    public AuthTokenResponse signup(SignupRequest request) {
        if (!request.password().equals(request.passwordConfirm())) {
            throw new BusinessException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        // 인증 완료 토큰을 확인하고, 토큰에 묶인 전화번호를 꺼낸다 (요청으로 받은 번호가 아니라 인증된 번호를 쓴다)
        String tokenKey = RedisKey.signupVerified(CryptoUtil.sha256Hex(request.verificationToken()));
        String phone = redis.opsForValue().get(tokenKey);
        if (phone == null) {
            throw new BusinessException(AuthErrorCode.PHONE_NOT_VERIFIED);
        }

        // 인증 이후에 같은 번호로 가입된 경우
        if (userRepository.existsByPhoneNumber(phone)) {
            throw new BusinessException(AuthErrorCode.PHONE_ALREADY_REGISTERED);
        }

        User user;
        try {
            user = userRepository.save(User.builder()
                    .name(request.name().trim())
                    .nickname(request.nickname())
                    .password(passwordEncoder.encode(request.password()))
                    .phoneNumber(phone)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 번호로 가입 요청이 오면 DB의 UNIQUE 제약이 마지막으로 막는다
            throw new BusinessException(AuthErrorCode.PHONE_ALREADY_REGISTERED);
        }

        // 가입 직후에는 로그인 유지로 처리해서 refresh 토큰을 14일간 둔다
        AuthTokenResponse response = tokenService.issueTokens(user, request.deviceId(), true);

        // 인증 완료 토큰은 DB 커밋이 성공한 뒤에만 삭제한다 (커밋이 실패하면 같은 토큰으로 다시 시도할 수 있다)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    redis.delete(tokenKey);
                } catch (Exception e) {
                    // 가입은 이미 끝났으므로 응답은 성공으로 두고, 토큰은 유효시간(30분)이 지나면 사라진다
                    log.warn("가입 인증 토큰 삭제에 실패했습니다.", e);
                }
            }
        });

        return response;
    }

    // 로그인
    public AuthTokenResponse login(LoginRequest request) {
        String phone = request.phoneNumber();

        // 잠긴 번호는 비밀번호를 확인하지 않고 바로 거절한다
        loginAttemptService.checkNotLocked(phone);

        // 없는 번호, 탈퇴한 계정, 틀린 비밀번호는 같은 오류로 응답한다 (가입 여부를 알아낼 수 없게 한다)
        User user = userRepository.findByPhoneNumberAndDeletedAtIsNull(phone).orElse(null);
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            loginAttemptService.recordFailure(phone);
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        loginAttemptService.clear(phone);
        return tokenService.issueTokens(user, request.deviceId(), request.keepLoggedIn());
    }

    // 토큰 재발급
    public AuthTokenResponse refresh(TokenRefreshRequest request) {
        Long userId = tokenService.extractUserId(request.refreshToken());

        // 탈퇴했거나 없는 계정이면 그 기기의 세션도 지우고 거절한다
        User user = userRepository.findById(userId).filter(u -> u.getDeletedAt() == null).orElse(null);
        if (user == null) {
            tokenService.revoke(userId, request.deviceId());
            throw new BusinessException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        return tokenService.rotate(user, request.deviceId(), request.refreshToken());
    }

    // 로그아웃: 이 기기의 refresh 토큰만 삭제한다. 이미 발급된 access 토큰은 만료(30분)될 때까지 유효하다
    public void logout(Long userId, LogoutRequest request) {
        tokenService.revoke(userId, request.deviceId());
    }
}