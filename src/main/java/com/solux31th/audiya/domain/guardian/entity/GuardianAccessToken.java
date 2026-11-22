package com.solux31th.audiya.domain.guardian.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "guardian_access_token")
@Getter
@NoArgsConstructor
public class GuardianAccessToken {
    @Id @Column(name = "guardian_access_token_id") private Long id;
    @Column(name = "token_hash", nullable = false, length = 64) private String tokenHash;
    @Column(nullable = false, length = 20) private String purpose;
    @Column(name = "recipient_phone_number", nullable = false) private String recipientPhoneNumber;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "revoked_at") private Instant revokedAt;
    @Column(name = "last_accessed_at") private Instant lastAccessedAt;
    @Column(name = "trip_id", nullable = false) private Long tripId;
    @Column(name = "emergency_contact_id", nullable = false) private Long contactId;
    @Column(name = "emergency_event_id") private Long emergencyEventId;
    public void accessedAt(Instant now) { lastAccessedAt = now; }
}
