package com.solux31th.audiya.domain.trip.service;

import com.solux31th.audiya.domain.route.entity.Route;
import com.solux31th.audiya.domain.route.entity.RouteType;
import com.solux31th.audiya.domain.route.repository.RouteRepository;
import com.solux31th.audiya.domain.trip.dto.request.TripCreateRequest;
import com.solux31th.audiya.domain.trip.dto.response.CurrentTripResponse;
import com.solux31th.audiya.domain.trip.dto.response.TripCreateResponse;
import com.solux31th.audiya.domain.trip.entity.Trip;
import com.solux31th.audiya.domain.trip.entity.TripStatus;
import com.solux31th.audiya.domain.trip.exception.TripErrorCode;
import com.solux31th.audiya.domain.trip.repository.TripRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
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
        given(tripRepository.existsByUserIdAndStatusInAndDeletedAtIsNull(eq(USER_ID), anyCollection())).willReturn(true);

        assertThatThrownBy(() -> tripService.createTrip(USER_ID, createRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(TripErrorCode.TRIP_ALREADY_IN_PROGRESS);
        verify(tripRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("귀가를 생성하면 요청한 사용자의 READY 귀가가 저장된다")
    void createTrip_success() {
        given(tripRepository.existsByUserIdAndStatusInAndDeletedAtIsNull(eq(USER_ID), anyCollection())).willReturn(false);
        given(tripRepository.saveAndFlush(any(Trip.class))).willAnswer(invocation -> {
            Trip saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", TRIP_ID);
            return saved;
        });

        TripCreateResponse response = tripService.createTrip(USER_ID, createRequest());

        assertThat(response.tripId()).isEqualTo(TRIP_ID);
        assertThat(response.status()).isEqualTo(TripStatus.READY);
        ArgumentCaptor<Trip> captor = ArgumentCaptor.forClass(Trip.class);
        verify(tripRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getDestinationName()).isEqualTo("우리집");
    }

    @Test
    @DisplayName("동시 요청으로 DB의 진행 중 귀가 UNIQUE 제약에 걸리면 TRIP_ALREADY_IN_PROGRESS")
    void createTrip_whenActiveUserUniqueViolated_throwsConflict() {
        given(tripRepository.existsByUserIdAndStatusInAndDeletedAtIsNull(eq(USER_ID), anyCollection())).willReturn(false);
        given(tripRepository.saveAndFlush(any(Trip.class))).willThrow(new DataIntegrityViolationException("insert failed",
                new SQLException("Duplicate entry '1' for key 'trip." + Trip.ACTIVE_USER_UNIQUE_KEY + "'")));

        assertThatThrownBy(() -> tripService.createTrip(USER_ID, createRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(TripErrorCode.TRIP_ALREADY_IN_PROGRESS);
    }

    @Test
    @DisplayName("다른 제약 위반은 그대로 던진다")
    void createTrip_whenOtherIntegrityViolation_rethrows() {
        given(tripRepository.existsByUserIdAndStatusInAndDeletedAtIsNull(eq(USER_ID), anyCollection())).willReturn(false);
        given(tripRepository.saveAndFlush(any(Trip.class))).willThrow(new DataIntegrityViolationException("insert failed",
                new SQLException("Column 'user_id' cannot be null")));

        assertThatThrownBy(() -> tripService.createTrip(USER_ID, createRequest()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("진행 중인 귀가가 없으면 null을 반환한다")
    void getCurrentTrip_whenNone_returnsNull() {
        given(tripRepository.findFirstByUserIdAndStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(eq(USER_ID), anyCollection()))
                .willReturn(Optional.empty());

        assertThat(tripService.getCurrentTrip(USER_ID)).isNull();
    }

    @Test
    @DisplayName("진행 중인 귀가가 있으면 귀가 정보와 선택한 경로 ID를 반환한다")
    void getCurrentTrip_success() {
        given(tripRepository.findFirstByUserIdAndStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(eq(USER_ID), anyCollection()))
                .willReturn(Optional.of(trip(USER_ID)));
        Route route = Route.builder().tripId(TRIP_ID).routeType(RouteType.SAFE).build();
        ReflectionTestUtils.setField(route, "id", 31L);
        given(routeRepository.findByTripIdAndSelectedTrue(TRIP_ID)).willReturn(Optional.of(route));

        CurrentTripResponse response = tripService.getCurrentTrip(USER_ID);

        assertThat(response.tripId()).isEqualTo(TRIP_ID);
        assertThat(response.status()).isEqualTo(TripStatus.READY);
        assertThat(response.destinationName()).isEqualTo("우리집");
        assertThat(response.routeId()).isEqualTo(31L);
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
