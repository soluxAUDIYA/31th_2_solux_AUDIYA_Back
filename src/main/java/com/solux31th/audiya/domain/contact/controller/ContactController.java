package com.solux31th.audiya.domain.contact.controller;

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
    @AuthenticationPrincipal Long userId,
    @Valid @RequestBody ContactCreateRequest request
  ) {
    if (userId == null) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }

    return ApiResponse.of(
      SuccessCode.CREATED,
      contactService.createContact(userId, request)
    );
  }

  @GetMapping
  public ApiResponse<List<ContactResponse>> getContacts(
    @AuthenticationPrincipal Long userId
  ) {
    requireUser(userId);

    return ApiResponse.ok(contactService.getContacts(userId));
  }

  @PutMapping("/{contactId}")
  public ApiResponse<ContactResponse> updateContact(
    @AuthenticationPrincipal Long userId,
    @PathVariable("contactId") Long contactId,
    @Valid @RequestBody ContactUpdateRequest request
  ) {
    requireUser(userId);

    return ApiResponse.ok(
      contactService.updateContact(userId, contactId, request)
    );
  }

  @DeleteMapping("/{contactId}")
  public ApiResponse<Void> deleteContact(
    @AuthenticationPrincipal Long userId,
    @PathVariable("contactId") Long contactId
  ) {
    requireUser(userId);

    contactService.deleteContact(userId, contactId);

    return ApiResponse.ok();
  }

  @PatchMapping("/{contactId}/sms-setting")
  public ApiResponse<ContactResponse> updateSmsSetting(
    @AuthenticationPrincipal Long userId,
    @PathVariable("contactId") Long contactId,
    @Valid @RequestBody ContactSmsSettingUpdateRequest request
  ) {
    requireUser(userId);

    return ApiResponse.ok(
      contactService.updateSmsSetting(userId, contactId, request)
    );
  }

  @PatchMapping("/{contactId}/priority")
  public ApiResponse<ContactResponse> updatePriority(
    @AuthenticationPrincipal Long userId,
    @PathVariable("contactId") Long contactId,
    @Valid @RequestBody ContactPriorityUpdateRequest request
  ) {
    requireUser(userId);
    return ApiResponse.ok(contactService.updatePriority(userId, contactId, request));
  }

  private void requireUser(Long userId) {
    if (userId == null) {
      throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
  }
}
