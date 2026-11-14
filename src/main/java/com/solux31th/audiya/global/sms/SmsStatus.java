package com.solux31th.audiya.global.sms;

// 문자 발송 상태. ERD sms_log.status 값과 동일하다.
// DELIVERED(전달 확인)와 UNKNOWN은 업체 연동 후 수신 결과 콜백에서 사용한다.
public enum SmsStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
    UNKNOWN
}