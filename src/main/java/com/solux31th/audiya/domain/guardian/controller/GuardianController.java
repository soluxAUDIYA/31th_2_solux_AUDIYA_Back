package com.solux31th.audiya.domain.guardian.controller;

import com.solux31th.audiya.domain.guardian.dto.GuardianSessionRequest;
import com.solux31th.audiya.domain.guardian.dto.GuardianSessionResponse;
import com.solux31th.audiya.domain.guardian.service.GuardianService;
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
    public GuardianController(GuardianService service) { this.service = service; }

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
