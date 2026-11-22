package com.solux31th.audiya.domain.trip.exception;

import com.solux31th.audiya.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

// trip 도메인 오류 코드
@Getter
@RequiredArgsConstructor
public enum TripErrorCode implements ErrorCode {

    TRIP_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "이미 진행 중인 귀가가 있습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public String getCode() {
        return name();
    }
}
