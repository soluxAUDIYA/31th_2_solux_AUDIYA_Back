package com.solux31th.audiya.global.sms;

// 문자 유형. ERD sms_log.message_type 값과 동일하다.
public enum SmsMessageType {
    SIGNUP_OTP(true),
    GUARDIAN_OTP(true),
    PASSWORD_RESET_OTP(true),
    EMERGENCY(false),
    ARRIVAL(false);

    // true면 본문에 인증번호가 들어 있으므로 이력에 원문을 저장하지 않는다
    private final boolean containsCode;

    SmsMessageType(boolean containsCode) {
        this.containsCode = containsCode;
    }

    public boolean containsCode() {
        return containsCode;
    }
}