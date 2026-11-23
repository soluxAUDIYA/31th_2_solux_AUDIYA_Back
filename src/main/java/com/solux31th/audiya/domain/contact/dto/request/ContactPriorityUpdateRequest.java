package com.solux31th.audiya.domain.contact.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record ContactPriorityUpdateRequest(
  @NotNull
  @Min(1)
  @Max(10)
  Integer priority
) {}
