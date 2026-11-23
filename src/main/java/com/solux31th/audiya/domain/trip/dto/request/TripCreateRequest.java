package com.solux31th.audiya.domain.trip.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// 귀가 생성 요청. 검증 메시지에는 오류 코드를 쓴다 (fieldErrors.reason)
public record TripCreateRequest(
        @Size(max = 100, message = "TOO_LONG")
        String originName,

        @NotNull(message = "REQUIRED")
        @DecimalMin(value = "-90", message = "INVALID_LATITUDE")
        @DecimalMax(value = "90", message = "INVALID_LATITUDE")
        BigDecimal originLat,

        @NotNull(message = "REQUIRED")
        @DecimalMin(value = "-180", message = "INVALID_LONGITUDE")
        @DecimalMax(value = "180", message = "INVALID_LONGITUDE")
        BigDecimal originLng,

        @Size(max = 100, message = "TOO_LONG")
        String destinationName,

        @NotNull(message = "REQUIRED")
        @DecimalMin(value = "-90", message = "INVALID_LATITUDE")
        @DecimalMax(value = "90", message = "INVALID_LATITUDE")
        BigDecimal destinationLat,

        @NotNull(message = "REQUIRED")
        @DecimalMin(value = "-180", message = "INVALID_LONGITUDE")
        @DecimalMax(value = "180", message = "INVALID_LONGITUDE")
        BigDecimal destinationLng
) {
}
