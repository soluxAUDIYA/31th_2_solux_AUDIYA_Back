package com.solux31th.audiya.domain.guardian.dto;
import java.time.Instant;
public record GuardianSessionResponse(boolean authenticated, String scope, Instant expiresAt) {}
