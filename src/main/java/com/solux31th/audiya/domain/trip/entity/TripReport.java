package com.solux31th.audiya.domain.trip.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 귀가 완료 리포트 (ERD trip_report). trip 하나당 하나만 생성된다.
@Getter
@Entity
@Table(name = "trip_report")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_report_id")
    private Long id;

    @Column(name = "trip_id", nullable = false, unique = true)
    private Long tripId;

    // 총 소요 시간 (초)
    @Column(name = "total_duration")
    private Integer totalDuration;

    // 총 이동 거리 (미터)
    @Column(name = "total_distance")
    private Integer totalDistance;

    @Column(name = "passed_cctv_count", nullable = false)
    private int passedCctvCount;

    @Column(name = "passed_light_count", nullable = false)
    private int passedLightCount;

    @Column(name = "arrived_at")
    private LocalDateTime arrivedAt;

    @Column(name = "arrival_notified", nullable = false)
    private boolean arrivalNotified;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private TripReport(Long tripId, Integer totalDuration, Integer totalDistance, int passedCctvCount,
                       int passedLightCount, LocalDateTime arrivedAt) {
        this.tripId = tripId;
        this.totalDuration = totalDuration;
        this.totalDistance = totalDistance;
        this.passedCctvCount = passedCctvCount;
        this.passedLightCount = passedLightCount;
        this.arrivedAt = arrivedAt;
        this.arrivalNotified = false;
    }
}
