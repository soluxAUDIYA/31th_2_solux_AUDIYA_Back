package com.solux31th.audiya.domain.auth.exception;

import com.solux31th.audiya.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 전화번호 인증
    PHONE_ALREADY_REGISTERED(HttpStatus.CONFLICT, "이미 가입된 전화번호입니다."),
    PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "휴대전화 인증을 완료해 주세요."),
    SMS_RESEND_TOO_SOON(HttpStatus.TOO_MANY_REQUESTS, "잠시 후 다시 요청해 주세요."),
    SMS_SEND_FAILED(HttpStatus.BAD_GATEWAY, "문자를 발송하지 못했습니다. 잠시 후 다시 시도해 주세요."),
    CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다."),
    CODE_EXPIRED(HttpStatus.BAD_REQUEST, "인증번호가 만료되었습니다. 다시 요청해 주세요."),
    CODE_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "인증번호 입력 횟수를 초과했습니다. 다시 요청해 주세요."),

    // 회원가입
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),

    // 로그인, 토큰
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "전화번호 또는 비밀번호가 올바르지 않습니다."),
    LOGIN_ATTEMPTS_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "로그인 시도 횟수를 초과했습니다. 5분 후 다시 시도해 주세요."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "로그인이 만료되었습니다. 다시 로그인해 주세요."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}