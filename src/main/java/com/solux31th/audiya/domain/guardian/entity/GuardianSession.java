package com.solux31th.audiya.domain.guardian.entity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.time.Instant;
@Entity
@Table(name = "guardian_session")
@NoArgsConstructor
public class GuardianSession {
    @Id @Column(name = "session_hash", length = 64) private String sessionHash;
    @Column(name = "guardian_access_token_id", nullable = false) private Long accessTokenId;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    public GuardianSession(String sessionHash, Long accessTokenId, Instant expiresAt) {
        this.sessionHash = sessionHash; this.accessTokenId = accessTokenId; this.expiresAt = expiresAt;
    }
}
