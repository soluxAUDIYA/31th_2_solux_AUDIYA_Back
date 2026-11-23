package com.solux31th.audiya.domain.guardian.service;

import com.solux31th.audiya.domain.guardian.dto.GuardianSessionResponse;
import com.solux31th.audiya.domain.guardian.entity.GuardianSession;
import com.solux31th.audiya.domain.guardian.exception.GuardianErrorCode;
import com.solux31th.audiya.domain.guardian.repository.GuardianAccessTokenRepository;
import com.solux31th.audiya.domain.guardian.repository.GuardianSessionRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class GuardianService {
    private final GuardianAccessTokenRepository tokens;
    private final GuardianSessionRepository sessions;
    private final ObjectProvider<GuardianAuthorization> authorization;
    private final SecureRandom random = new SecureRandom();

    public GuardianService(GuardianAccessTokenRepository tokens, GuardianSessionRepository sessions,
                           ObjectProvider<GuardianAuthorization> authorization) {
        this.tokens = tokens;
        this.sessions = sessions;
        this.authorization = authorization;
    }

    @Transactional
    public IssuedSession createSession(String rawToken) {
        var token = tokens.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new BusinessException(GuardianErrorCode.INVALID_LINK));
        Instant now = Instant.now();
        if (token.getRevokedAt() != null) throw new BusinessException(GuardianErrorCode.INVALID_LINK);
        if (!now.isBefore(token.getExpiresAt())) throw new BusinessException(GuardianErrorCode.EXPIRED_LINK);
        if (!("EMERGENCY".equals(token.getPurpose()) || "TRIP_REPORT".equals(token.getPurpose()))
                || token.getTripId() == null || token.getContactId() == null
                || ("EMERGENCY".equals(token.getPurpose()) && token.getEmergencyEventId() == null)) {
            throw new BusinessException(GuardianErrorCode.INVALID_LINK);
        }
        GuardianAuthorization checker = authorization.getIfAvailable();
        if (checker == null) throw new BusinessException(GuardianErrorCode.AUTHORIZATION_UNAVAILABLE);
        if (!checker.isAuthorized(token)) throw new BusinessException(GuardianErrorCode.INVALID_LINK);
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = now.plus(1, ChronoUnit.HOURS);
        if (expiresAt.isAfter(token.getExpiresAt())) expiresAt = token.getExpiresAt();
        sessions.save(new GuardianSession(hash(secret), token.getId(), expiresAt));
        token.accessedAt(now);
        return new IssuedSession(secret, new GuardianSessionResponse(true, token.getPurpose(), expiresAt));
    }

    static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public record IssuedSession(String secret, GuardianSessionResponse response) {}
}
