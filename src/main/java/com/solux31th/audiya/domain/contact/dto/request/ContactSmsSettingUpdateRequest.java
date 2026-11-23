package com.solux31th.audiya.domain.contact.dto.request;

import jakarta.validation.constraints.NotNull;

public record ContactSmsSettingUpdateRequest(
  @NotNull
  Boolean smsEnabled
) {}