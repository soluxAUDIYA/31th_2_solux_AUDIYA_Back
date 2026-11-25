package com.solux31th.audiya.domain.contact.exception;

import com.solux31th.audiya.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ContactException implements ErrorCode {

  CONTACT_NOT_FOUND(
    HttpStatus.NOT_FOUND,
    "비상연락처를 찾을 수 없습니다."
  ),
  CONTACT_LIMIT_EXCEEDED(
    HttpStatus.BAD_REQUEST,
    "비상연락처는 최대 10개까지 등록할 수 있습니다."
  ),
  INVALID_CONTACT_PRIORITY(
    HttpStatus.BAD_REQUEST,
    "현재 연락처 개수 범위 안의 우선순위를 지정해주세요."
  );

  private final HttpStatus status;
  private final String message;

  @Override
  public String getCode() {
    return name();
  }
}
