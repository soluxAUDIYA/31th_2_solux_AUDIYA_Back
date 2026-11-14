package com.solux31th.audiya.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solux31th.audiya.global.exception.ErrorCode;

import java.util.List;

// 모든 API가 사용하는 공통 응답 형식
// 성공: { "success": true,  "code": "OK", "message": "...", "data": { ... } }
// 실패: { "success": false, "code": "ERROR_CODE", "message": "...", "data": null }
// 입력 검증 실패: 실패 응답에 "fieldErrors": [ { "field": "...", "reason": "..." } ] 추가
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<FieldError> fieldErrors
) {

    private static final String OK_CODE = "OK";
    private static final String OK_MESSAGE = "요청이 처리되었습니다.";

    // 데이터가 있는 성공 응답
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, OK_CODE, OK_MESSAGE, data, null);
    }

    // 데이터가 없는 성공 응답 (data: null)
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, OK_CODE, OK_MESSAGE, null, null);
    }

    // 오류 코드의 기본 메시지를 사용하는 실패 응답
    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null, null);
    }

    // 메시지를 직접 지정하는 실패 응답
    public static ApiResponse<Void> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, errorCode.getCode(), message, null, null);
    }

    // 입력 검증 실패 응답 (필드별 오류 포함)
    public static ApiResponse<Void> error(ErrorCode errorCode, List<FieldError> fieldErrors) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null, fieldErrors);
    }

    // 입력 검증 실패 시 필드별 오류. reason에는 검증 어노테이션의 message(오류 코드)가 들어간다.
    public record FieldError(String field, String reason) {
    }
}