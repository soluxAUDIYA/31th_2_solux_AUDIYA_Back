package com.solux31th.audiya.domain.trip.service;

import com.solux31th.audiya.domain.route.repository.RouteRepository;
import com.solux31th.audiya.domain.trip.dto.request.TripCreateRequest;
import com.solux31th.audiya.domain.trip.entity.Trip;
import com.solux31th.audiya.domain.trip.exception.TripErrorCode;
import com.solux31th.audiya.domain.trip.repository.TripRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long TRIP_ID = 10L;

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private TripService tripService;

    @Test
    @DisplayName("진행 중인 귀가가 있으면 새 귀가를 만들 수 없다")
    void createTrip_whenActiveTripExists_throwsConflict() {
        given(tripRepository.existsByUserIdAndStatusInAndDeletedAtIsNull(any(), anyCollection())).willReturn(true);

        assertThatThrownBy(() -> tripService.createTrip(USER_ID, createRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(TripErrorCode.TRIP_ALREADY_IN_PROGRESS);
        verify(tripRepository, never()).save(any());
    }

    @Test
    @DisplayName("진행 중인 귀가가 없으면 null을 반환한다")
    void getCurrentTrip_whenNone_returnsNull() {
        given(tripRepository.findFirstByUserIdAndStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(anyLong(), anyCollection()))
                .willReturn(Optional.empty());

        assertThat(tripService.getCurrentTrip(USER_ID)).isNull();
    }

    private Trip trip(Long userId) {
        Trip trip = Trip.builder()
                .userId(userId)
                .originName("숙명여대 정문")
                .originLat(new BigDecimal("37.5465"))
                .originLng(new BigDecimal("126.9648"))
                .destinationName("우리집")
                .destinationLat(new BigDecimal("37.5571"))
                .destinationLng(new BigDecimal("126.9450"))
                .build();
        ReflectionTestUtils.setField(trip, "id", TRIP_ID);
        return trip;
    }

    private TripCreateRequest createRequest() {
        return new TripCreateRequest("숙명여대 정문", new BigDecimal("37.5465"), new BigDecimal("126.9648"),
                "우리집", new BigDecimal("37.5571"), new BigDecimal("126.9450"));
    }
}
