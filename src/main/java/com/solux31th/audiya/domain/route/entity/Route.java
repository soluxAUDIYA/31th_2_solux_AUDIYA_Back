package com.solux31th.audiya.domain.route.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 귀가 세션에서 탐색한 경로 후보 (ERD route). trip은 연관관계 대신 ID만 보관한다.
@Getter
@Entity
@Table(name = "route")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id")
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Enumerated(EnumType.STRING)
    @Column(name = "route_type", nullable = false, length = 20)
    private RouteType routeType;

    // 총 거리 (미터)
    @Column(name = "total_distance")
    private Integer totalDistance;

    // 예상 소요 시간 (초)
    @Column(name = "total_time")
    private Integer totalTime;

    @Column(name = "safety_score", precision = 5, scale = 2)
    private BigDecimal safetyScore;

    @Column(name = "cctv_count")
    private Integer cctvCount;

    @Column(name = "bright_ratio", precision = 5, scale = 2)
    private BigDecimal brightRatio;

    @Column(name = "has_accident_zone", nullable = false)
    private boolean hasAccidentZone;

    // 경로 좌표 목록 (JSON 문자열)
    @Column(columnDefinition = "json")
    private String path;

    @Column(name = "is_selected", nullable = false)
    private boolean selected;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private Route(Long tripId, RouteType routeType, Integer totalDistance, Integer totalTime,
                  BigDecimal safetyScore, Integer cctvCount, BigDecimal brightRatio,
                  boolean hasAccidentZone, String path) {
        this.tripId = tripId;
        this.routeType = routeType;
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.safetyScore = safetyScore;
        this.cctvCount = cctvCount;
        this.brightRatio = brightRatio;
        this.hasAccidentZone = hasAccidentZone;
        this.path = path;
        this.selected = false;
    }
}
