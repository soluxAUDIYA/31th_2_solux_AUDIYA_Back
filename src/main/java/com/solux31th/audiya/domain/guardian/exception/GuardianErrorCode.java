package com.solux31th.audiya.domain.guardian.exception;
import com.solux31th.audiya.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@Getter
@RequiredArgsConstructor
public enum GuardianErrorCode implements ErrorCode {
    INVALID_LINK(HttpStatus.UNAUTHORIZED, "유효하지 않은 보호자 링크입니다."),
    EXPIRED_LINK(HttpStatus.GONE, "만료된 보호자 링크입니다."),
    AUTHORIZATION_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "보호자 권한을 확인할 수 없습니다.");
    private final HttpStatus status;
    private final String message;
    public String getCode() { return name(); }
}
