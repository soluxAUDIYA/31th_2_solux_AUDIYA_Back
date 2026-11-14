package com.solux31th.audiya.global.exception;

import com.solux31th.audiya.global.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 컨트롤러에서 발생한 예외를 공통 응답 형식(ApiResponse)으로 바꿔주는 곳.
 * 여기서 처리하지 못한 예외는 마지막 handleException에서 500으로 응답한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 서비스에서 던진 BusinessException */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("BusinessException: {} - {}", errorCode.getCode(), e.getMessage());
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode, e.getMessage()));
    }

    /** @RequestBody + @Valid 검증 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBodyValidation(MethodArgumentNotValidException e) {
        List<ApiResponse.FieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return validationFailed(fieldErrors);
    }

    /** @RequestParam, @PathVariable 등에 붙인 검증 실패 (컨트롤러 메서드 파라미터 검증) */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodValidation(HandlerMethodValidationException e) {
        List<ApiResponse.FieldError> fieldErrors = new ArrayList<>();
        e.getParameterValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(error ->
                        fieldErrors.add(new ApiResponse.FieldError(
                                result.getMethodParameter().getParameterName(),
                                error.getDefaultMessage()))));
        return validationFailed(fieldErrors);
    }

    /** @Validated 가 붙은 클래스에서 발생하는 검증 실패 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        List<ApiResponse.FieldError> fieldErrors = e.getConstraintViolations().stream()
                .map(v -> new ApiResponse.FieldError(lastNodeName(v.getPropertyPath().toString()), v.getMessage()))
                .toList();
        return validationFailed(fieldErrors);
    }

    /** JSON 형식 오류, 필수 파라미터 누락, 파라미터 타입 불일치 */
    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequest(Exception e) {
        log.warn("Malformed request: {}", e.getMessage());
        return response(CommonErrorCode.MALFORMED_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return response(CommonErrorCode.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return response(CommonErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    /** 존재하지 않는 경로 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return response(CommonErrorCode.NOT_FOUND);
    }

    /** 위에서 처리하지 못한 모든 예외. 원인은 로그에만 남기고 응답에는 노출하지 않는다. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return response(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ApiResponse<Void>> validationFailed(List<ApiResponse.FieldError> fieldErrors) {
        return ResponseEntity
                .status(CommonErrorCode.VALIDATION_FAILED.getStatus())
                .body(ApiResponse.error(CommonErrorCode.VALIDATION_FAILED, fieldErrors));
    }

    private ResponseEntity<ApiResponse<Void>> response(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus()).body(ApiResponse.error(errorCode));
    }

    /** "signup.request.name" 처럼 경로로 오는 이름에서 마지막 필드명만 꺼낸다. */
    private String lastNodeName(String propertyPath) {
        int idx = propertyPath.lastIndexOf('.');
        return idx >= 0 ? propertyPath.substring(idx + 1) : propertyPath;
    }
}