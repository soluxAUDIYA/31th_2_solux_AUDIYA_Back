package com.solux31th.audiya.global.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 다른 도메인이 문자를 보낼 때 호출하는 진입점
 *
 * 처리 순서: 이력 저장(PENDING) -> 발송 -> 결과 반영(SENT/FAILED).
 * 외부 업체를 호출하는 동안 DB 트랜잭션을 잡지 않도록 이 클래스에는 @Transactional을 쓰지 않는다.
 *
 * 같은 requestKey로 다시 호출하면 (재시도)
 * - 이미 SENT/DELIVERED: 다시 보내지 않고 성공 결과를 돌려준다 (중복 발송 방지)
 * - PENDING: 다른 요청이 처리 중이므로 SMS_IN_PROGRESS 실패로 돌려준다
 * - FAILED/UNKNOWN: 같은 이력으로 다시 발송하고 attempt_count를 올린다
 *
 * 발송이 실패해도 예외를 던지지 않고 결과로 돌려준다. 실패 처리는 호출한 도메인이 정한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private static final String CODE_MASK = "******";

    private final SmsSender smsSender;
    private final SmsLogRepository smsLogRepository;

    public SmsSendResult send(SmsSendRequest request) {
        SmsLog smsLog;

        Optional<SmsLog> existing = smsLogRepository.findByRequestKey(request.requestKey());
        if (existing.isPresent()) {
            smsLog = existing.get();
            switch (smsLog.getStatus()) {
                case SENT, DELIVERED -> {
                    return SmsSendResult.success(smsLog.getProviderMessageId());
                }
                case PENDING -> {
                    return SmsSendResult.fail("SMS_IN_PROGRESS", "이미 처리 중인 발송 요청입니다.");
                }
                default -> { /* FAILED, UNKNOWN: 아래에서 재시도 */ }
            }
        } else {
            try {
                smsLog = smsLogRepository.save(SmsLog.builder()
                        .guardianAccessTokenId(request.guardianAccessTokenId())
                        .messageType(request.type())
                        .recipientPhoneNumber(request.phoneNumber())
                        .messageContent(toLogContent(request.type(), request.content()))
                        .requestKey(request.requestKey())
                        .build());
            } catch (DataIntegrityViolationException e) {
                // 동시에 같은 requestKey로 들어온 경우 (UNIQUE 충돌)
                return SmsSendResult.fail("SMS_IN_PROGRESS", "이미 처리 중인 발송 요청입니다.");
            }
        }

        smsLog.startAttempt(smsSender.providerName());
        smsLog = smsLogRepository.save(smsLog);

        SmsSendResult result;
        try {
            result = smsSender.send(request.phoneNumber(), request.content());
        } catch (Exception e) {
            // 업체 호출 중 예외가 나도 서버 오류로 번지지 않게 실패 결과로 바꾼다 (예외 내용은 이력에 남기지 않는다)
            log.warn("SMS send error. to={}, type={}", PhoneMasker.mask(request.phoneNumber()), request.type(), e);
            result = SmsSendResult.fail("PROVIDER_ERROR", "문자 제공자 호출에 실패했습니다.");
        }

        if (result.success()) {
            smsLog.markSent(result.providerMessageId());
        } else {
            smsLog.markFailed(result.errorCode(), result.errorMessage());
        }
        smsLogRepository.save(smsLog);

        return result;
    }

    // 이력에 저장할 본문. 인증번호 유형은 4~8자리 숫자를, 모든 유형에서 16자 이상의 토큰 문자열 (보호자 링크 토큰 등)을 가린다.
    private String toLogContent(SmsMessageType type, String content) {
        if (content == null) {
            return null;
        }
        String masked = content;
        if (type.containsCode()) {
            masked = masked.replaceAll("\\d{4,8}", CODE_MASK);
        }
        return masked.replaceAll("[A-Za-z0-9_-]{16,}", CODE_MASK);
    }
}