package com.solux31th.audiya.global.sms;

// 문자 발송 요청
public record SmsSendRequest(
        String phoneNumber,          // 수신 번호 (숫자만)
        SmsMessageType type,         // 문자 유형
        String content,          // 발송 본문 (인증번호·링크 토큰이 있어도 되며, 이력에는 마스킹되어 저장된다)
        String requestKey,     // 발송 요청 멱등 키. 같은 요청의 재시도에는 같은 키를 쓴다 (예: "SIGNUP_OTP:" + UUID)
        Long guardianAccessTokenId   // 보호자 링크 문자일 때만 사용, 그 외는 null
) {

    // 보호자 토큰과 무관한 일반 문자 (인증번호 등)
    public static SmsSendRequest of(String phoneNumber, SmsMessageType type, String content, String requestKey) {
        return new SmsSendRequest(phoneNumber, type, content, requestKey, null);
    }
}