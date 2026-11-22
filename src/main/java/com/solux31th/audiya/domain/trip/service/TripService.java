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
        // TODO: 동시에 두 요청이 들어오면 둘 다 통과할 수 있다. 필요하면 사용자 단위 락 등으로 보완한다.
        if (tripRepository.existsByUserIdAndStatusInAndDeletedAtIsNull(userId, ACTIVE_STATUSES)) {
            throw new BusinessException(TripErrorCode.TRIP_ALREADY_IN_PROGRESS);
        }

        Trip trip = tripRepository.save(Trip.builder()
                .userId(userId)
                .originName(request.originName())
                .originLat(request.originLat())
                .originLng(request.originLng())
                .destinationName(request.destinationName())
                .destinationLat(request.destinationLat())
                .destinationLng(request.destinationLng())
                .build());

        return TripCreateResponse.from(trip);
    }

    // 진행 중인 귀가가 없으면 null
    public CurrentTripResponse getCurrentTrip(Long userId) {
        return tripRepository
                .findFirstByUserIdAndStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(userId, ACTIVE_STATUSES)
                .map(trip -> CurrentTripResponse.of(trip, findSelectedRouteId(trip.getId())))
                .orElse(null);
    }

    private Long findSelectedRouteId(Long tripId) {
        return routeRepository.findByTripIdAndSelectedTrue(tripId)
                .map(Route::getId)
                .orElse(null);
    }
}
