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
    AUTHORIZATION_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "보호자 권한을 확인할 수 없습니다."),
    INVALID_SESSION(HttpStatus.UNAUTHORIZED, "보호자 세션이 유효하지 않습니다."),
    FORBIDDEN_SCOPE(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    EXPIRED_SESSION(HttpStatus.GONE, "보호자 접근이 만료되었습니다."),
    DATA_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "조회 정보를 확인할 수 없습니다."),
    INVALID_PAGINATION(HttpStatus.BAD_REQUEST, "조회 조건이 올바르지 않습니다."),
    TRIP_NOT_FOUND(HttpStatus.NOT_FOUND, "귀가 정보를 찾을 수 없습니다."),
    TRIP_NOT_COMPLETED(HttpStatus.CONFLICT, "귀가가 완료되지 않았습니다.");
    private final HttpStatus status;
    private final String message;
    public String getCode() { return name(); }
}
