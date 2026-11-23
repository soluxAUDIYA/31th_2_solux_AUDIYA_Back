package com.solux31th.audiya.domain.contact.controller;

import com.solux31th.audiya.domain.auth.entity.User;
import com.solux31th.audiya.domain.contact.dto.request.ContactCreateRequest;
import com.solux31th.audiya.domain.contact.dto.request.ContactUpdateRequest;
import com.solux31th.audiya.domain.contact.dto.response.ContactResponse;
import com.solux31th.audiya.domain.contact.dto.request.ContactSmsSettingUpdateRequest;
import com.solux31th.audiya.domain.contact.service.ContactService;
import com.solux31th.audiya.global.exception.BusinessException;
import com.solux31th.audiya.global.exception.CommonErrorCode;
import com.solux31th.audiya.global.response.ApiResponse;
import com.solux31th.audiya.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.solux31th.audiya.domain.contact.dto.request.ContactPriorityUpdateRequest;

// User의 엔티티 매핑 완성되어야 JPA 연동 가능
@RestController
@RequiredArgsConstructor
@RequestMapping("/contacts")
public class ContactController {

  private final ContactService contactService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<ContactResponse> createContact(
    @AuthenticationPrincipal User user,
    @Valid @RequestBody ContactCreateRequest request
  ) {
    requireUser(user);

    return ApiResponse.of(
      SuccessCode.CREATED,
      contactService.createContact(user, request)
    );
  }

  @GetMapping
  public ApiResponse<List<ContactResponse>> getContacts(
    @AuthenticationPrincipal User user
  ) {
    requireUser(user);

    return ApiResponse.ok(contactService.getContacts(user));
  }

  @PutMapping("/{contactId}")
  public ApiResponse<ContactResponse> updateContact(
    @AuthenticationPrincipal User user,
    @PathVariable("contactId") Long contactId,
    @Valid @RequestBody ContactUpdateRequest request
  ) {
    requireUser(user);

    return ApiResponse.ok(
      contactService.updateContact(user, contactId, request)
    );
  }

  @DeleteMapping("/{contactId}")
  public ApiResponse<Void> deleteContact(
    @AuthenticationPrincipal User user,
    @PathVariable("contactId") Long contactId
  ) {
    requireUser(user);

    contactService.deleteContact(user, contactId);

    return ApiResponse.ok();
  }

  @PatchMapping("/{contactId}/sms-setting")
  public ApiResponse<ContactResponse> updateSmsSetting(
    @AuthenticationPrincipal User user,
    @PathVariable("contactId") Long contactId,
    @Valid @RequestBody ContactSmsSettingUpdateRequest request
  ) {
    requireUser(user);

    return ApiResponse.ok(
      contactService.updateSmsSetting(user, contactId, request)
    );
  }

  @PatchMapping("/{contactId}/priority")
  public ApiResponse<ContactResponse> updatePriority(
    @AuthenticationPrincipal User user,
    @PathVariable("contactId") Long contactId,
    @Valid @RequestBody ContactPriorityUpdateRequest request
  ) {
    requireUser(user);
    return ApiResponse.ok(contactService.updatePriority(user, contactId, request));
  }

  private void requireUser(User user) {
    if (user == null) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
  }
}
