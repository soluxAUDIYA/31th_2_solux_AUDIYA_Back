package com.solux31th.audiya.global.exception;

import lombok.Getter;

// 서비스 로직에서 "예상 가능한 실패"를 알릴 때 던지는 예외
// 예) throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 기본 메시지 대신 상황에 맞는 메시지를 직접 지정할 때
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}