package com.solux31th.audiya.domain.auth.service;

import com.solux31th.audiya.domain.auth.dto.request.SignupRequest;
import com.solux31th.audiya.domain.auth.dto.response.AuthTokenResponse;
import com.solux31th.audiya.domain.auth.entity.User;
import com.solux31th.audiya.domain.auth.exception.AuthErrorCode;
import com.solux31th.audiya.domain.auth.redis.RedisKey;
import com.solux31th.audiya.domain.auth.repository.UserRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import com.solux31th.audiya.global.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redis;
    private final TokenService tokenService;

    // 회원가입
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

        // 인증 완료 토큰은 1회용이므로 가입이 끝나면 삭제한다
        redis.delete(tokenKey);

        // 가입 직후에는 로그인 유지로 처리해서 refresh 토큰을 14일간 둔다
        return tokenService.issueTokens(user, request.deviceId(), true);
    }
}