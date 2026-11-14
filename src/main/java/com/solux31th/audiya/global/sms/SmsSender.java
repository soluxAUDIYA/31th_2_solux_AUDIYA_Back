package com.solux31th.audiya.global.sms;

// 실제 문자 발송을 담당하는 인터페이스.
// 업체가 정해지면 이 인터페이스를 구현한 클래스를 추가하고 LocalSmsSender를 대체한다.
// 도메인에서는 직접 쓰지 않고 SmsService를 통해서만 사용한다.
public interface SmsSender {

    // sms_log.provider에 기록되는 업체 이름
    String providerName();

    SmsSendResult send(String to, String content);
}