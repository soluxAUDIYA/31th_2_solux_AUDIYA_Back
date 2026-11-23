package com.solux31th.audiya.domain.guardian.service;

import com.solux31th.audiya.domain.guardian.entity.GuardianAccessToken;
import com.solux31th.audiya.domain.guardian.exception.GuardianErrorCode;
import com.solux31th.audiya.domain.guardian.repository.GuardianAccessTokenRepository;
import com.solux31th.audiya.domain.guardian.repository.GuardianSessionRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GuardianServiceTest {
    GuardianAccessTokenRepository tokens = mock(GuardianAccessTokenRepository.class);
    GuardianSessionRepository sessions = mock(GuardianSessionRepository.class);
    @SuppressWarnings("unchecked") ObjectProvider<GuardianAuthorization> authorization = mock(ObjectProvider.class);
    GuardianAuthorization checker = mock(GuardianAuthorization.class);
    GuardianService service = new GuardianService(tokens, sessions, authorization);
    GuardianAccessToken token;

    @BeforeEach
    void setup() {
        token = new GuardianAccessToken();
        ReflectionTestUtils.setField(token, "id", 1L);
        ReflectionTestUtils.setField(token, "tripId", 2L);
        ReflectionTestUtils.setField(token, "contactId", 3L);
        ReflectionTestUtils.setField(token, "purpose", "TRIP_REPORT");
        ReflectionTestUtils.setField(token, "expiresAt", Instant.now().plusSeconds(600));
        when(tokens.findByTokenHash(anyString())).thenReturn(Optional.of(token));
    }

    @Test void blocksWhenAuthorizationIsUnavailable() {
        var error = assertThrows(BusinessException.class, () -> service.createSession("link"));
        assertEquals(GuardianErrorCode.AUTHORIZATION_UNAVAILABLE, error.getErrorCode());
        verifyNoInteractions(sessions);
    }

    @Test void rejectsExpiredAndRevokedLinks() {
        ReflectionTestUtils.setField(token, "expiresAt", Instant.now().minusSeconds(1));
        assertEquals(GuardianErrorCode.EXPIRED_LINK, assertThrows(BusinessException.class, () -> service.createSession("link")).getErrorCode());
        ReflectionTestUtils.setField(token, "revokedAt", Instant.now());
        assertEquals(GuardianErrorCode.INVALID_LINK, assertThrows(BusinessException.class, () -> service.createSession("link")).getErrorCode());
        verifyNoInteractions(sessions);
    }

    @Test void issuesSessionOnlyAfterAuthorizationAndLimitsExpiry() {
        when(authorization.getIfAvailable()).thenReturn(checker);
        when(checker.isAuthorized(token)).thenReturn(true);
        var issued = service.createSession("link");
        assertTrue(issued.response().authenticated());
        assertEquals(token.getExpiresAt(), issued.response().expiresAt());
        verify(tokens).findByTokenHash(GuardianService.hash("link"));
        verify(sessions).save(any());
        assertNotNull(token.getLastAccessedAt());
    }

    @Test void rejectsUnauthorizedContact() {
        when(authorization.getIfAvailable()).thenReturn(checker);
        assertEquals(GuardianErrorCode.INVALID_LINK, assertThrows(BusinessException.class, () -> service.createSession("link")).getErrorCode());
        verifyNoInteractions(sessions);
    }
}
