package com.solux31th.audiya.domain.guardian.dto;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;

public record GuardianAlertResponse(String notificationType, User user, Emergency emergency, Location location,
                                    String tripStatus, List<String> availableViews) {
    public record User(String name) {}
    public record Emergency(String state, List<String> triggerTypes, String checkInStatus, Instant requestedAt) {}
    public record Location(BigDecimal latitude, BigDecimal longitude, Instant recordedAt, String address,
                           String placeName, String locationStatus) {}
}
