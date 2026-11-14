package com.solux31th.audiya.global.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

// 로컬 개발용 발송기. 실제로 보내지 않고 로그만 출력한다.
// 인증번호를 눈으로 확인할 수 있도록 본문은 로그에 남기되, 전화번호는 마스킹한다.
// 운영에서는 실제 업체 구현체로 교체해야 한다 (본문을 로그에 남기지 말 것)
@Slf4j
@Component
public class LocalSmsSender implements SmsSender {

    @Override
    public String providerName() {
        return "LOCAL";
    }

    @Override
    public SmsSendResult send(String to, String content) {
        log.info("[LOCAL SMS] to={}, content={}", PhoneMasker.mask(to), content);
        return SmsSendResult.success("LOCAL-" + UUID.randomUUID());
    }
}