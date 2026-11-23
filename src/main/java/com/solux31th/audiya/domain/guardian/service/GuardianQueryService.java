package com.solux31th.audiya.domain.guardian.service;

import com.solux31th.audiya.domain.guardian.dto.GuardianAlertResponse;
import com.solux31th.audiya.domain.guardian.dto.GuardianSituationResponse;
import com.solux31th.audiya.domain.guardian.dto.GuardianReportResponse;
import com.solux31th.audiya.domain.guardian.entity.GuardianAccessToken;
import com.solux31th.audiya.domain.guardian.exception.GuardianErrorCode;
import com.solux31th.audiya.domain.guardian.repository.GuardianAccessTokenRepository;
import com.solux31th.audiya.domain.guardian.repository.GuardianSessionRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class GuardianQueryService {
    private final GuardianSessionRepository sessions;
    private final GuardianAccessTokenRepository tokens;
    private final ObjectProvider<GuardianAuthorization> authorization;
    private final ObjectProvider<GuardianQuerySource> source;
    private final ObjectProvider<GuardianReportSource> reportSource;

    public GuardianQueryService(GuardianSessionRepository sessions, GuardianAccessTokenRepository tokens,
                                ObjectProvider<GuardianAuthorization> authorization, ObjectProvider<GuardianQuerySource> source,
                                ObjectProvider<GuardianReportSource> reportSource) {
        this.sessions = sessions;
        this.tokens = tokens;
        this.authorization = authorization;
        this.source = source;
        this.reportSource = reportSource;
    }

    @Transactional(readOnly = true)
    public GuardianAlertResponse currentAlert(String secret) {
        GuardianAccessToken token = authenticate(secret);
        return querySource().getCurrentAlert(token);
    }

    @Transactional(readOnly = true)
    public GuardianSituationResponse situation(String secret, String cursor, int limit) {
        if (limit < 1 || limit > 100 || (cursor != null && (cursor.isBlank() || cursor.length() > 512))) {
            throw new BusinessException(GuardianErrorCode.INVALID_PAGINATION);
        }
        GuardianAccessToken token = authenticate(secret);
        if (!"EMERGENCY".equals(token.getPurpose()) || token.getEmergencyEventId() == null) {
            throw new BusinessException(GuardianErrorCode.FORBIDDEN_SCOPE);
        }
        return querySource().getSituation(token, cursor, limit);
    }

    @Transactional(readOnly = true)
    public GuardianReportResponse report(String secret) {
        GuardianAccessToken token = authenticate(secret);
        if (!"TRIP_REPORT".equals(token.getPurpose())) {
            throw new BusinessException(GuardianErrorCode.FORBIDDEN_SCOPE);
        }
        GuardianReportSource provider = reportSource.getIfAvailable();
        if (provider == null) throw new BusinessException(GuardianErrorCode.DATA_UNAVAILABLE);
        GuardianReportSource.ReportResult result = provider.getReport(token);
        if (result == null || result.status() == null) throw new BusinessException(GuardianErrorCode.DATA_UNAVAILABLE);
        return switch (result.status()) {
            case NOT_FOUND -> throw new BusinessException(GuardianErrorCode.TRIP_NOT_FOUND);
            case NOT_COMPLETED -> throw new BusinessException(GuardianErrorCode.TRIP_NOT_COMPLETED);
            case PENDING -> null;
            case READY -> {
                if (result.report() == null || !"COMPLETED".equals(result.report().tripStatus())) {
                    throw new BusinessException(GuardianErrorCode.DATA_UNAVAILABLE);
                }
                yield result.report();
            }
        };
    }

    private GuardianAccessToken authenticate(String secret) {
        if (secret == null || secret.isBlank() || secret.length() > 512) {
            throw new BusinessException(GuardianErrorCode.INVALID_SESSION);
        }
        var session = sessions.findById(GuardianService.hash(secret))
                .orElseThrow(() -> new BusinessException(GuardianErrorCode.INVALID_SESSION));
        Instant now = Instant.now();
        if (session.getExpiresAt() == null || !now.isBefore(session.getExpiresAt())) {
            throw new BusinessException(GuardianErrorCode.EXPIRED_SESSION);
        }
        var token = tokens.findById(session.getAccessTokenId())
                .orElseThrow(() -> new BusinessException(GuardianErrorCode.FORBIDDEN_SCOPE));
        if (token.getRevokedAt() != null) throw new BusinessException(GuardianErrorCode.FORBIDDEN_SCOPE);
        if (token.getExpiresAt() == null || !now.isBefore(token.getExpiresAt())) {
            throw new BusinessException(GuardianErrorCode.EXPIRED_SESSION);
        }
        if (token.getTripId() == null || token.getContactId() == null
                || !("EMERGENCY".equals(token.getPurpose()) || "TRIP_REPORT".equals(token.getPurpose()))
                || ("EMERGENCY".equals(token.getPurpose()) && token.getEmergencyEventId() == null)) {
            throw new BusinessException(GuardianErrorCode.FORBIDDEN_SCOPE);
        }
        GuardianAuthorization checker = authorization.getIfAvailable();
        if (checker == null) throw new BusinessException(GuardianErrorCode.AUTHORIZATION_UNAVAILABLE);
        if (!checker.isAuthorized(token)) throw new BusinessException(GuardianErrorCode.FORBIDDEN_SCOPE);
        return token;
    }

    private GuardianQuerySource querySource() {
        GuardianQuerySource result = source.getIfAvailable();
        if (result == null) throw new BusinessException(GuardianErrorCode.DATA_UNAVAILABLE);
        return result;
    }
}
