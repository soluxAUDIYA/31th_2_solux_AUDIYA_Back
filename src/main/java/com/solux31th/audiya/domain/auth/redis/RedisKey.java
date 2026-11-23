package com.solux31th.audiya.domain.auth.redis;

public final class RedisKey {

    private RedisKey() {
    }

    // 가입 인증번호 (해시, 실패 횟수)
    public static String signupOtp(String phone) {
        return "otp:signup:" + phone;
    }

    // 가입 인증번호 재전송 잠금
    public static String signupResend(String phone) {
        return "otp:signup:resend:" + phone;
    }

    // 가입 인증 완료 토큰 (값: 인증된 전화번호)
    public static String signupVerified(String tokenHash) {
        return "signup:verified:" + tokenHash;
    }

    // 번호별 일일 SMS 발송 횟수
    public static String smsDaily(String phone) {
        return "sms:daily:" + phone;
    }
}