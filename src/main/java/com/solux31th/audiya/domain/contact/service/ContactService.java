package com.solux31th.audiya.domain.contact.service;

import com.solux31th.audiya.domain.auth.entity.User;
import com.solux31th.audiya.domain.auth.repository.UserRepository;
import com.solux31th.audiya.domain.contact.dto.request.ContactCreateRequest;
import com.solux31th.audiya.domain.contact.dto.response.ContactResponse;
import com.solux31th.audiya.domain.contact.entity.Contact;
import com.solux31th.audiya.domain.contact.repository.ContactRepository;
import com.solux31th.audiya.domain.contact.dto.request.ContactUpdateRequest;
import com.solux31th.audiya.domain.contact.dto.request.ContactSmsSettingUpdateRequest;
import com.solux31th.audiya.domain.contact.exception.ContactException;
import com.solux31th.audiya.global.exception.BusinessException;
import com.solux31th.audiya.global.exception.CommonErrorCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.solux31th.audiya.domain.contact.dto.request.ContactPriorityUpdateRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactService {

  private static final int MAX_CONTACTS = 10;

  private final ContactRepository contactRepository;
  private final UserRepository userRepository;

  @Transactional
  public ContactResponse createContact(
    Long userId,
    ContactCreateRequest request
  ) {

    User user = findUser(userId);

    long contactCount = contactRepository.countByUserAndDeletedAtIsNull(user);
    if (contactCount >= MAX_CONTACTS) {
      throw new BusinessException(ContactException.CONTACT_LIMIT_EXCEEDED);
    }

    Contact contact = new Contact(
      user,
      request.name(),
      request.phoneNumber(),
      (int) contactCount + 1
    );

    Contact savedContact = contactRepository.save(contact);

    return ContactResponse.from(savedContact);
  }

  private User findUser(Long userId) {
    return userRepository.findById(userId)
    .filter(user -> user.getDeletedAt() == null)
    .orElseThrow(() ->
      new BusinessException(CommonErrorCode.UNAUTHORIZED)
    );
  }

  public List<ContactResponse> getContacts(Long userId) {
    User user = findUser(userId);

    return contactRepository
      .findAllByUserAndDeletedAtIsNullOrderByPriorityAsc(user)
      .stream()
      .map(ContactResponse::from)
      .toList();
  }
  // 연락처 조회
  private Contact findContact(Long contactId, User user) {
    return contactRepository
      .findByIdAndUserAndDeletedAtIsNull(contactId, user)
      .orElseThrow(() ->
      new BusinessException(ContactException.CONTACT_NOT_FOUND)
    );
  }
  // 연락처 수정
  @Transactional
  public ContactResponse updateContact(
    Long userId,
    Long contactId,
    ContactUpdateRequest request
  ) {
    User user = findUser(userId);
    Contact contact = findContact(contactId, user);

    contact.updateContact(
      request.name(),
      request.phoneNumber()
    );

    return ContactResponse.from(contact);
  }

  // 연락처 삭제
  @Transactional
  public void deleteContact(Long userId, Long contactId) {
    User user = findUser(userId);
    Contact contact = findContact(contactId, user);

    int deletedPriority = contact.getPriority();
    contact.delete();

    for (Contact remaining : contactRepository.findAllByUserAndDeletedAtIsNullOrderByPriorityAsc(user)) {
      if (remaining.getPriority() > deletedPriority) {
        remaining.changePriority(remaining.getPriority() - 1);
      }
    }
  }

  // sms 수신 변경
  @Transactional
  public ContactResponse updateSmsSetting(
    Long userId,
    Long contactId,
    ContactSmsSettingUpdateRequest request
  ) {
    User user = findUser(userId);
    Contact contact = findContact(contactId, user);
    contact.changeSmsEnabled(request.smsEnabled());

    return ContactResponse.from(contact);
  }

  // 우선순위 변경
  @Transactional
  public ContactResponse updatePriority(
    Long userId,
    Long contactId,
    ContactPriorityUpdateRequest request
  ) {
    User user = findUser(userId);
    Contact contact = findContact(contactId, user);
    List<Contact> contacts = contactRepository
      .findAllByUserAndDeletedAtIsNullOrderByPriorityAsc(user);
    Integer newPriority = request.priority();
    if (newPriority == null || newPriority < 1 || newPriority > contacts.size()) {
      throw new BusinessException(ContactException.INVALID_CONTACT_PRIORITY);
    }

    int oldPriority = contact.getPriority();
    for (Contact other : contacts) {
      int priority = other.getPriority();
      if (newPriority < oldPriority && priority >= newPriority && priority < oldPriority) {
        other.changePriority(priority + 1);
      } else if (newPriority > oldPriority && priority > oldPriority && priority <= newPriority) {
        other.changePriority(priority - 1);
      }
    }
    contact.changePriority(newPriority);
    return ContactResponse.from(contact);
  }
}
