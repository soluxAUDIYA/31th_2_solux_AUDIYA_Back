package com.solux31th.audiya.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    OK(HttpStatus.OK, "요청이 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "생성되었습니다."),
    ACCEPTED(HttpStatus.ACCEPTED, "요청이 접수되었습니다.");

    private final HttpStatus status;
    private final String message;

    public String getCode() {
        return name();
    }
}