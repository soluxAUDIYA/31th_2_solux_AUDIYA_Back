package com.solux31th.audiya.global.sms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 문자 발송 이력 (ERD sms_log). 인증번호와 링크 토큰 원문은 저장하지 않는다.
// guardian_access_token은 다른 도메인 엔티티이므로 연관관계 대신 ID만 보관한다.
@Getter
@Entity
@Table(name = "sms_log")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SmsLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sms_log_id")
    private Long id;

    @Column(name = "guardian_access_token_id")
    private Long guardianAccessTokenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 30)
    private SmsMessageType messageType;

    @Column(name = "recipient_phone_number", nullable = false, length = 20)
    private String recipientPhoneNumber;

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SmsStatus status;

    @Column(name = "request_key", nullable = false, unique = true, length = 100)
    private String requestKey;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(length = 30)
    private String provider;

    @Column(name = "provider_message_id", length = 100)
    private String providerMessageId;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "last_attempted_at")
    private LocalDateTime lastAttemptedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private SmsLog(Long guardianAccessTokenId, SmsMessageType messageType, String recipientPhoneNumber,
                   String messageContent, String requestKey) {
        this.guardianAccessTokenId = guardianAccessTokenId;
        this.messageType = messageType;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.messageContent = messageContent;
        this.requestKey = requestKey;
        this.status = SmsStatus.PENDING;
        this.attemptCount = 0;
        this.requestedAt = LocalDateTime.now();
    }

    // 발송 시도 시작: 시도 횟수 증가, 상태를 PENDING으로 되돌린다 (재시도 포함)
    public void startAttempt(String provider) {
        this.status = SmsStatus.PENDING;
        this.provider = provider;
        this.attemptCount++;
        this.lastAttemptedAt = LocalDateTime.now();
        this.errorCode = null;
        this.errorMessage = null;
    }

    public void markSent(String providerMessageId) {
        this.status = SmsStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.sentAt = LocalDateTime.now();
    }

    public void markFailed(String errorCode, String errorMessage) {
        this.status = SmsStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}