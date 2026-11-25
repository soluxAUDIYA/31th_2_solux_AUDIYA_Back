package com.solux31th.audiya.domain.contact.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactUpdateRequest(
  @NotBlank
  @Size(max = 50)
  String name,

  @NotBlank
  @Size(max = 15)
  String phoneNumber
) {}
