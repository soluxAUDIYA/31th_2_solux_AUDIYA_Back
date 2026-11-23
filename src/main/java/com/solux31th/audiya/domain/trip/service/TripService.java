package com.solux31th.audiya.domain.trip.service;

import com.solux31th.audiya.domain.route.entity.Route;
import com.solux31th.audiya.domain.route.repository.RouteRepository;
import com.solux31th.audiya.domain.trip.dto.request.TripCreateRequest;
import com.solux31th.audiya.domain.trip.dto.response.CurrentTripResponse;
import com.solux31th.audiya.domain.trip.dto.response.TripCreateResponse;
import com.solux31th.audiya.domain.trip.entity.Trip;
import com.solux31th.audiya.domain.trip.entity.TripStatus;
import com.solux31th.audiya.domain.trip.exception.TripErrorCode;
import com.solux31th.audiya.domain.trip.repository.TripRepository;
import com.solux31th.audiya.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    // 진행 중으로 보는 상태. 사용자당 이 상태의 귀가는 1건만 허용한다.
    private static final List<TripStatus> ACTIVE_STATUSES = List.of(TripStatus.READY, TripStatus.IN_PROGRESS);

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;

    @Transactional
    public TripCreateResponse createTrip(Long userId, TripCreateRequest request) {
        if (tripRepository.existsByUserIdAndStatusInAndDeletedAtIsNull(userId, ACTIVE_STATUSES)) {
            throw new BusinessException(TripErrorCode.TRIP_ALREADY_IN_PROGRESS);
        }

        Trip trip = Trip.builder()
                .userId(userId)
                .originName(request.originName())
                .originLat(request.originLat())
                .originLng(request.originLng())
                .destinationName(request.destinationName())
                .destinationLat(request.destinationLat())
                .destinationLng(request.destinationLng())
                .build();

        try {
            // 위 확인을 동시에 통과한 요청은 DB의 UNIQUE 제약(uk_trip_active_user_id)에서 막힌다.
            return TripCreateResponse.from(tripRepository.saveAndFlush(trip));
        } catch (DataIntegrityViolationException e) {
            if (isActiveTripConflict(e)) {
                throw new BusinessException(TripErrorCode.TRIP_ALREADY_IN_PROGRESS);
            }
            throw e;
        }
    }

    // 진행 중인 귀가가 없으면 null
    public CurrentTripResponse getCurrentTrip(Long userId) {
        return tripRepository
                .findFirstByUserIdAndStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(userId, ACTIVE_STATUSES)
                .map(trip -> CurrentTripResponse.of(trip, findSelectedRouteId(trip.getId())))
                .orElse(null);
    }

    private boolean isActiveTripConflict(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        return message != null && message.contains(Trip.ACTIVE_USER_UNIQUE_KEY);
    }

    private Long findSelectedRouteId(Long tripId) {
        return routeRepository.findByTripIdAndSelectedTrue(tripId)
                .map(Route::getId)
                .orElse(null);
    }
}
