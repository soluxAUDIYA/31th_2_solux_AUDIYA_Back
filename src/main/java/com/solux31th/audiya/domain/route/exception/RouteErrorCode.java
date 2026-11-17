package com.solux31th.audiya.domain.route.exception;

import com.solux31th.audiya.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// route 도메인 오류 코드. 기능 구현 시 상수를 추가한다.
@Getter
@RequiredArgsConstructor
public enum RouteErrorCode implements ErrorCode {
    ;

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
