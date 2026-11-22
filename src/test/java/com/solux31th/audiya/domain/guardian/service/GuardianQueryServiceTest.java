package com.solux31th.audiya.domain.guardian.service;

import com.solux31th.audiya.domain.guardian.dto.GuardianAlertResponse;
import com.solux31th.audiya.domain.guardian.entity.GuardianAccessToken;
import com.solux31th.audiya.domain.guardian.entity.GuardianSession;
import com.solux31th.audiya.domain.guardian.exception.GuardianErrorCode;
import com.solux31th.audiya.domain.guardian.repository.GuardianAccessTokenRepository;
import com.solux31th.audiya.domain.guardian.repository.GuardianSessionRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GuardianQueryServiceTest {
    GuardianSessionRepository sessions = mock(GuardianSessionRepository.class);
    GuardianAccessTokenRepository tokens = mock(GuardianAccessTokenRepository.class);
    @SuppressWarnings("unchecked") ObjectProvider<GuardianAuthorization> auth = mock(ObjectProvider.class);
    @SuppressWarnings("unchecked") ObjectProvider<GuardianQuerySource> source = mock(ObjectProvider.class);
    GuardianAuthorization checker = mock(GuardianAuthorization.class);
    GuardianQuerySource data = mock(GuardianQuerySource.class);
    GuardianQueryService service = new GuardianQueryService(sessions, tokens, auth, source);
    GuardianAccessToken token;

    @BeforeEach void setup() {
        token = new GuardianAccessToken();
        ReflectionTestUtils.setField(token, "id", 1L);
        ReflectionTestUtils.setField(token, "tripId", 2L);
        ReflectionTestUtils.setField(token, "contactId", 3L);
        ReflectionTestUtils.setField(token, "emergencyEventId", 4L);
        ReflectionTestUtils.setField(token, "purpose", "EMERGENCY");
        ReflectionTestUtils.setField(token, "expiresAt", Instant.now().plusSeconds(600));
        when(sessions.findById(anyString())).thenReturn(Optional.of(new GuardianSession("hash", 1L, Instant.now().plusSeconds(600))));
        when(tokens.findById(1L)).thenReturn(Optional.of(token));
        when(auth.getIfAvailable()).thenReturn(checker);
        when(checker.isAuthorized(token)).thenReturn(true);
        when(source.getIfAvailable()).thenReturn(data);
    }

    @Test void authorizedAlertUsesLinkedToken() {
        var response = new GuardianAlertResponse("SOS", new GuardianAlertResponse.User("사용자"), null, null, "IN_PROGRESS", List.of("SITUATION"));
        when(data.getCurrentAlert(token)).thenReturn(response);
        assertEquals(response, service.currentAlert("secret"));
        verify(sessions).findById(GuardianService.hash("secret"));
        verify(data).getCurrentAlert(token);
    }

    @Test void missingAndExpiredSessionsAreRejected() {
        assertEquals(GuardianErrorCode.INVALID_SESSION, assertThrows(BusinessException.class, () -> service.currentAlert(null)).getErrorCode());
        when(sessions.findById(anyString())).thenReturn(Optional.of(new GuardianSession("hash", 1L, Instant.now().minusSeconds(1))));
        assertEquals(GuardianErrorCode.EXPIRED_SESSION, assertThrows(BusinessException.class, () -> service.currentAlert("secret")).getErrorCode());
        verifyNoInteractions(data);
    }

    @Test void revokedOrExpiredLinkIsRejected() {
        ReflectionTestUtils.setField(token, "revokedAt", Instant.now());
        assertEquals(GuardianErrorCode.FORBIDDEN_SCOPE, assertThrows(BusinessException.class, () -> service.currentAlert("secret")).getErrorCode());
        ReflectionTestUtils.setField(token, "revokedAt", null);
        ReflectionTestUtils.setField(token, "expiresAt", Instant.now().minusSeconds(1));
        assertEquals(GuardianErrorCode.EXPIRED_SESSION, assertThrows(BusinessException.class, () -> service.currentAlert("secret")).getErrorCode());
        verifyNoInteractions(data);
    }

    @Test void reportLinkCannotReadSituation() {
        ReflectionTestUtils.setField(token, "purpose", "TRIP_REPORT");
        assertEquals(GuardianErrorCode.FORBIDDEN_SCOPE, assertThrows(BusinessException.class, () -> service.situation("secret", null, 20)).getErrorCode());
        verifyNoInteractions(data);
    }

    @Test void unavailableAuthorizationOrDataCannotExposeInformation() {
        when(auth.getIfAvailable()).thenReturn(null);
        assertEquals(GuardianErrorCode.AUTHORIZATION_UNAVAILABLE, assertThrows(BusinessException.class, () -> service.currentAlert("secret")).getErrorCode());
        when(auth.getIfAvailable()).thenReturn(checker);
        when(source.getIfAvailable()).thenReturn(null);
        assertEquals(GuardianErrorCode.DATA_UNAVAILABLE, assertThrows(BusinessException.class, () -> service.currentAlert("secret")).getErrorCode());
        verifyNoInteractions(data);
    }

    @Test void invalidPageLimitsAreRejected() {
        assertEquals(GuardianErrorCode.INVALID_PAGINATION, assertThrows(BusinessException.class, () -> service.situation("secret", null, 101)).getErrorCode());
        assertEquals(GuardianErrorCode.INVALID_PAGINATION, assertThrows(BusinessException.class, () -> service.situation("secret", " ", 20)).getErrorCode());
        verifyNoInteractions(data);
    }
}
