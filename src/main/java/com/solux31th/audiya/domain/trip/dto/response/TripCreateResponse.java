package com.solux31th.audiya.domain.trip.dto.response;

import com.solux31th.audiya.domain.trip.entity.Trip;
import com.solux31th.audiya.domain.trip.entity.TripStatus;

import java.time.LocalDateTime;

public record TripCreateResponse(
        Long tripId,
        TripStatus status,
        LocalDateTime createdAt
) {
    public static TripCreateResponse from(Trip trip) {
        return new TripCreateResponse(trip.getId(), trip.getStatus(), trip.getCreatedAt());
    }
}
