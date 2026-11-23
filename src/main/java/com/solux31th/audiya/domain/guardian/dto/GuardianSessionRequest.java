package com.solux31th.audiya.domain.guardian.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record GuardianSessionRequest(@NotBlank @Size(max = 512) String token) {}
