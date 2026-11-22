package com.solux31th.audiya.domain.trip.dto.response;

import com.solux31th.audiya.domain.trip.entity.Trip;
import com.solux31th.audiya.domain.trip.entity.TripStatus;

import java.time.LocalDateTime;

// 진행 중(READY 또는 IN_PROGRESS) 귀가. routeId는 경로를 선택하기 전이면 null
public record CurrentTripResponse(
        Long tripId,
        TripStatus status,
        String destinationName,
        Long routeId,
        LocalDateTime startedAt
) {
    public static CurrentTripResponse of(Trip trip, Long routeId) {
        return new CurrentTripResponse(trip.getId(), trip.getStatus(), trip.getDestinationName(),
                routeId, trip.getStartedAt());
    }
}
