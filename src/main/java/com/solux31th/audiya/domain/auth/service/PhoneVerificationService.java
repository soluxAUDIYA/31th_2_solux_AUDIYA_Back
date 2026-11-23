package com.solux31th.audiya.domain.auth.service;

import com.solux31th.audiya.domain.auth.dto.request.PhoneVerificationConfirmRequest;
import com.solux31th.audiya.domain.auth.dto.request.PhoneVerificationRequest;
import com.solux31th.audiya.domain.auth.dto.response.PhoneVerificationSendResponse;
import com.solux31th.audiya.domain.auth.dto.response.VerificationTokenResponse;
import com.solux31th.audiya.domain.auth.exception.AuthErrorCode;
import com.solux31th.audiya.domain.auth.redis.RedisKey;
import com.solux31th.audiya.domain.auth.repository.UserRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import com.solux31th.audiya.global.exception.CommonErrorCode;
import com.solux31th.audiya.global.sms.SmsMessageType;
import com.solux31th.audiya.global.sms.SmsSendRequest;
import com.solux31th.audiya.global.sms.SmsSendResult;
import com.solux31th.audiya.global.sms.SmsService;
import com.solux31th.audiya.global.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

// 가입 전화번호 인증 (인증번호 발송, 확인)
// Redis 저장 구조
// - 인증번호: Hash {code: 해시, fails: 실패 횟수}, 3분
// - 재전송 잠금: 30초
// - 일일 발송 횟수: 24시간
// - 인증 완료 토큰: 키는 토큰 해시, 값은 인증된 전화번호, 30분
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(30);
    private static final Duration DAILY_WINDOW = Duration.ofHours(24);
    private static final Duration VERIFIED_TOKEN_TTL = Duration.ofMinutes(30);
    private static final int DAILY_LIMIT = 10;
    private static final int MAX_FAILS = 5;
    private static final Duration IP_WINDOW = Duration.ofHours(1);
    private static final int IP_LIMIT = 20;

    private static final String FIELD_CODE = "code";
    private static final String FIELD_FAILS = "fails";

    private final SmsService smsService;
    private final UserRepository userRepository;
    private final StringRedisTemplate redis;

    // 가입 인증번호 발송
    public PhoneVerificationSendResponse sendSignupCode(PhoneVerificationRequest request, String clientIp) {
        String phone = request.phoneNumber();

        // IP별 요청 횟수 제한: 번호를 바꿔가며 요청하는 대량 발송을 막는다
        String ipKey = RedisKey.smsIp(clientIp);
        if (incrementWithExpire(ipKey, IP_WINDOW) > IP_LIMIT) {
            throw new BusinessException(CommonErrorCode.TOO_MANY_REQUESTS);
        }

        // 이미 가입된 번호는 거절
        if (userRepository.existsByPhoneNumber(phone)) {
            throw new BusinessException(AuthErrorCode.PHONE_ALREADY_REGISTERED);
        }

        // 일일 발송 한도 확인
        String dailyKey = RedisKey.smsDaily(phone);
        String daily = redis.opsForValue().get(dailyKey);
        if (daily != null && Integer.parseInt(daily) >= DAILY_LIMIT) {
            throw new BusinessException(CommonErrorCode.TOO_MANY_REQUESTS);
        }

        // 재전송 잠금: 이미 있으면 30초가 지나지 않은 것
        String resendKey = RedisKey.signupResend(phone);
        Boolean locked = redis.opsForValue().setIfAbsent(resendKey, "1", RESEND_INTERVAL);
        if (!Boolean.TRUE.equals(locked)) {
            throw new BusinessException(AuthErrorCode.SMS_RESEND_TOO_SOON);
        }

        // 새 번호를 저장하면 기존 번호와 실패 횟수가 함께 초기화된다
        String otpKey = RedisKey.signupOtp(phone);
        String code = CryptoUtil.randomCode6();
        // 인증번호와 실패 횟수를 한 번에 저장해서 둘 중 하나만 있는 상태를 만들지 않는다 (기존 값은 덮어쓴다)
        redis.opsForHash().putAll(otpKey, Map.of(
                FIELD_CODE, hashCode(phone, code),
                FIELD_FAILS, "0"));
        redis.expire(otpKey, CODE_TTL);

        // 재전송은 새 요청이므로 requestKey를 매번 새로 만든다
        SmsSendResult result = smsService.send(SmsSendRequest.of(
                phone,
                SmsMessageType.SIGNUP_OTP,
                "[어디야] 인증번호는 " + code + "입니다. 3분 안에 입력해 주세요.",
                SmsMessageType.SIGNUP_OTP.name() + ":" + UUID.randomUUID()));

        // 발송에 실패하면 저장한 번호와 잠금을 지워서 바로 다시 요청할 수 있게 한다
        if (!result.success()) {
            redis.delete(otpKey);
            redis.delete(resendKey);
            throw new BusinessException(AuthErrorCode.SMS_SEND_FAILED);
        }

        // 발송에 성공한 경우에만 일일 횟수를 올린다
        incrementWithExpire(dailyKey, DAILY_WINDOW);

        return new PhoneVerificationSendResponse((int) CODE_TTL.toSeconds(), (int) RESEND_INTERVAL.toSeconds());
    }

    // 가입 인증번호 확인 -> 인증 완료 토큰 발급
    public VerificationTokenResponse confirmSignupCode(PhoneVerificationConfirmRequest request) {
        String phone = request.phoneNumber();
        String otpKey = RedisKey.signupOtp(phone);

        Map<Object, Object> saved = redis.opsForHash().entries(otpKey);
        String savedHash = (String) saved.get(FIELD_CODE);
        if (savedHash == null || saved.get(FIELD_FAILS) == null) {
            // 3분이 지났거나 발송 이력이 없거나, 값이 비정상인 경우: 남은 값을 지우고 만료로 처리한다
            redis.delete(otpKey);
            throw new BusinessException(AuthErrorCode.CODE_EXPIRED);
        }

        // 비교하기 전에 시도 횟수를 원자적으로 먼저 올린다 (동시 요청이 와도 비교는 최대 5회)
        Long attempts = redis.opsForHash().increment(otpKey, FIELD_FAILS, 1);
        if (attempts == null || attempts > MAX_FAILS) {
            throw new BusinessException(AuthErrorCode.CODE_ATTEMPTS_EXCEEDED);
        }

        if (!savedHash.equals(hashCode(phone, request.code()))) {
            // 5회째 실패에서는 번호를 삭제해 재발송을 요구한다
            if (attempts >= MAX_FAILS) {
                redis.delete(otpKey);
                throw new BusinessException(AuthErrorCode.CODE_ATTEMPTS_EXCEEDED);
            }
            throw new BusinessException(AuthErrorCode.CODE_MISMATCH);
        }

        // 삭제에 성공한 요청 하나만 토큰을 발급한다 (동시에 맞는 번호가 와도 토큰은 하나)
        if (!Boolean.TRUE.equals(redis.delete(otpKey))) {
            throw new BusinessException(AuthErrorCode.CODE_EXPIRED);
        }

        // 인증 완료 토큰은 이 전화번호에 묶어서 저장한다 (원문 대신 해시를 키로 사용)
        String token = CryptoUtil.randomToken();
        redis.opsForValue().set(RedisKey.signupVerified(CryptoUtil.sha256Hex(token)), phone, VERIFIED_TOKEN_TTL);

        return new VerificationTokenResponse(token, (int) VERIFIED_TOKEN_TTL.toSeconds());
    }

    // 6자리 숫자는 해시만으로는 약해서 전화번호를 섞어 저장하고, 실패 횟수 제한으로 보완한다
    private String hashCode(String phone, String code) {
        return CryptoUtil.sha256Hex(phone + ":" + code);
    }

    private long incrementWithExpire(String key, Duration ttl) {
        redis.opsForValue().setIfAbsent(key, "0", ttl);
        Long count = redis.opsForValue().increment(key);
        return count == null ? 0 : count;
    }
}