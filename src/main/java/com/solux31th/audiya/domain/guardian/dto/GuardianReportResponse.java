package com.solux31th.audiya.domain.guardian.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record GuardianReportResponse(User user, String tripStatus, Instant arrivedAt, Location arrivalLocation,
                                     Route route, Report report, String completionMessage, DataStatus dataStatus) {
    public record User(String name) {}
    public record Location(BigDecimal latitude, BigDecimal longitude) {}
    public record Route(List<List<BigDecimal>> path, String pathType) {}
    public record Report(Long totalDuration, Long totalDistance, Integer passedCctvCount, Integer passedLightCount) {}
    public record DataStatus(String route, String message) {}
}
