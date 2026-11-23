package com.solux31th.audiya.domain.guardian.controller;

import com.solux31th.audiya.domain.guardian.dto.GuardianSessionResponse;
import com.solux31th.audiya.domain.guardian.exception.GuardianErrorCode;
import com.solux31th.audiya.domain.guardian.filter.GuardianCacheControlFilter;
import com.solux31th.audiya.domain.guardian.service.GuardianService;
import com.solux31th.audiya.domain.guardian.service.GuardianQueryService;
import com.solux31th.audiya.global.exception.BusinessException;
import com.solux31th.audiya.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GuardianControllerTest {
    private final GuardianService service = mock(GuardianService.class);
    private final GuardianQueryService query = mock(GuardianQueryService.class);
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new GuardianController(service, query))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new GuardianCacheControlFilter()).build();
    }

    @Test
    void issuesSecureCookieWithoutReturningSecretInBody() throws Exception {
        when(service.createSession("link")).thenReturn(new GuardianService.IssuedSession("session-secret",
                new GuardianSessionResponse(true, "EMERGENCY", Instant.now().plusSeconds(600))));
        mvc.perform(post("/guardian/sessions").servletPath("/guardian/sessions").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"link\"}"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("Secure")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")))
                .andExpect(jsonPath("$.data.authenticated").value(true))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("session-secret"))));
    }

    @Test
    void validationAndAuthorizationErrorsAreNotCached() throws Exception {
        mvc.perform(post("/guardian/sessions").servletPath("/guardian/sessions").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(header().string("Cache-Control", "no-store"));
        verify(service, never()).createSession(anyString());
        when(service.createSession("link")).thenThrow(new BusinessException(GuardianErrorCode.AUTHORIZATION_UNAVAILABLE));
        mvc.perform(post("/guardian/sessions").servletPath("/guardian/sessions").contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"link\"}"))
                .andExpect(status().isServiceUnavailable()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }
    @Test
    void pendingReportReturnsAcceptedAndRetryAfter() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/guardian/report")
                        .servletPath("/guardian/report").cookie(new jakarta.servlet.http.Cookie("guardian_session", "secret")))
                .andExpect(status().isAccepted()).andExpect(header().string("Retry-After", "30"))
                .andExpect(header().string("Cache-Control", "no-store"));
        verify(query).report("secret");
    }
}
