package com.solux31th.audiya.global.exception;

import org.springframework.http.HttpStatus;

// 모든 오류 코드가 구현하는 인터페이스.
// 공통 오류는 CommonErrorCode, 도메인별 오류는 각 도메인의 enum(예: AuthErrorCode)이 구현한다.
public interface ErrorCode {

    String getCode();
    HttpStatus getStatus();
    String getMessage();
}