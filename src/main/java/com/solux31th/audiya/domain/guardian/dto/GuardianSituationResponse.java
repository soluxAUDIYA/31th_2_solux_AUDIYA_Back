package com.solux31th.audiya.domain.guardian.dto;

import java.time.Instant;
import java.util.List;

public record GuardianSituationResponse(User user, String tripStatus, Emergency emergency,
                                        GuardianAlertResponse.Location location, List<TimelineItem> timeline,
                                        String nextCursor) {
    public record User(String name, String phoneNumber) {}
    public record Emergency(Long emergencyEventId, String state, List<String> triggerTypes, String checkInStatus,
                            String resolutionReason) {}
    public record TimelineItem(Long id, String type, Instant occurredAt) {}
}
