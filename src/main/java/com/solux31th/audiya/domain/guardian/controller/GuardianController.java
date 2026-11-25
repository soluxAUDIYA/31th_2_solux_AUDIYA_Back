package com.solux31th.audiya.domain.guardian.controller;

import com.solux31th.audiya.domain.guardian.dto.GuardianSessionRequest;
import com.solux31th.audiya.domain.guardian.dto.GuardianSessionResponse;
import com.solux31th.audiya.domain.guardian.service.GuardianService;
import com.solux31th.audiya.domain.guardian.service.GuardianQueryService;
import com.solux31th.audiya.domain.guardian.dto.GuardianAlertResponse;
import com.solux31th.audiya.domain.guardian.dto.GuardianSituationResponse;
import com.solux31th.audiya.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/guardian")
public class GuardianController {
    private final GuardianService service;
    private final GuardianQueryService queryService;
    public GuardianController(GuardianService service, GuardianQueryService queryService) {
        this.service = service;
        this.queryService = queryService;
    }

    @GetMapping("/alerts/current")
    public ApiResponse<GuardianAlertResponse> currentAlert(@CookieValue(value = "guardian_session", required = false) String session) {
        return ApiResponse.ok(queryService.currentAlert(session));
    }

    @GetMapping("/situation")
    public ApiResponse<GuardianSituationResponse> situation(@CookieValue(value = "guardian_session", required = false) String session,
                                                           @RequestParam(required = false) String cursor,
                                                           @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.ok(queryService.situation(session, cursor, limit));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<GuardianSessionResponse>> createSession(@Valid @RequestBody GuardianSessionRequest request) {
        var issued = service.createSession(request.token());
        long seconds = Math.max(0, Duration.between(Instant.now(), issued.response().expiresAt()).toSeconds());
        var cookie = ResponseCookie.from("guardian_session", issued.secret()).httpOnly(true).secure(true)
                .sameSite("Lax").path("/guardian").maxAge(Duration.ofSeconds(seconds)).build();
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.SET_COOKIE, cookie.toString()).body(ApiResponse.ok(issued.response()));
    }
}
