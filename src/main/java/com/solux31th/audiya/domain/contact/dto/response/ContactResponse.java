package com.solux31th.audiya.domain.contact.dto.response;

import com.solux31th.audiya.domain.contact.entity.Contact;

public record ContactResponse(
  Long id,
  String name,
  String phoneNumber,
  Integer priority,
  Boolean smsEnabled,
  Boolean isVerified
) {
  public static ContactResponse from(Contact contact) {
    return new ContactResponse(
      contact.getId(),
      contact.getName(),
      contact.getPhoneNumber(),
      contact.getPriority(),
      contact.getSmsEnabled(),
      contact.getIsVerified()
    );
  }
}
