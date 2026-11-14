package com.solux31th.audiya.global.sms;

// 문자 발송 결과.
public record SmsSendResult(
        boolean success,           // 발송(업체 접수) 성공 여부
        String providerMessageId,  // 업체 메시지 ID (실패 시 null)
        String errorCode,          // 실패 코드 (성공 시 null)
        String errorMessage        // 실패 사유 (성공 시 null, 인증정보 포함 금지)
) {

    public static SmsSendResult success(String providerMessageId) {
        return new SmsSendResult(true, providerMessageId, null, null);
    }

    public static SmsSendResult fail(String errorCode, String errorMessage) {
        return new SmsSendResult(false, null, errorCode, errorMessage);
    }
}