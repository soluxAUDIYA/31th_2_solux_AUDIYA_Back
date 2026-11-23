package com.solux31th.audiya.domain.trip.controller;

import com.solux31th.audiya.domain.trip.dto.request.TripCreateRequest;
import com.solux31th.audiya.domain.trip.dto.response.CurrentTripResponse;
import com.solux31th.audiya.domain.trip.dto.response.TripCreateResponse;
import com.solux31th.audiya.domain.trip.service.TripService;
import com.solux31th.audiya.global.exception.BusinessException;
import com.solux31th.audiya.global.exception.CommonErrorCode;
import com.solux31th.audiya.global.response.ApiResponse;
import com.solux31th.audiya.global.response.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    // TODO: JWT 인증 구현 후 제거. 그 전까지 local 프로필에서만 이 헤더로 사용자를 지정해 테스트한다.
    private static final String TEST_USER_ID_HEADER = "X-User-Id";

    private final TripService tripService;
    private final Environment environment;

    @PostMapping
    public ResponseEntity<ApiResponse<TripCreateResponse>> createTrip(
            @AuthenticationPrincipal Long principalUserId,
            @RequestHeader(value = TEST_USER_ID_HEADER, required = false) Long testUserId,
            @Valid @RequestBody TripCreateRequest request) {
        TripCreateResponse data = tripService.createTrip(resolveUserId(principalUserId, testUserId), request);
        return ResponseEntity
                .status(SuccessCode.CREATED.getStatus())
                .body(ApiResponse.of(SuccessCode.CREATED, data));
    }

    @GetMapping("/current")
    public ApiResponse<CurrentTripResponse> getCurrentTrip(
            @AuthenticationPrincipal Long principalUserId,
            @RequestHeader(value = TEST_USER_ID_HEADER, required = false) Long testUserId) {
        return ApiResponse.ok(tripService.getCurrentTrip(resolveUserId(principalUserId, testUserId)));
    }

    // 인증된 사용자 ID를 우선 사용한다. 인증 정보가 없으면 local 프로필에서만 테스트 헤더를 허용한다.
    private Long resolveUserId(Long principalUserId, Long testUserId) {
        if (principalUserId != null) {
            return principalUserId;
        }
        if (testUserId != null && environment.matchesProfiles("local")) {
            return testUserId;
        }
        throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
    }
}
