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

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 귀가 중 기록되는 위치 (ERD trip_location). updated_at이 없어 BaseTimeEntity를 상속하지 않는다.
@Getter
@Entity
@Table(name = "trip_location")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TripLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_location_id")
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    // 위치 정확도 (미터)
    private Float accuracy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private TripLocation(Long tripId, BigDecimal latitude, BigDecimal longitude, Float accuracy,
                         LocalDateTime recordedAt) {
        this.tripId = tripId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.recordedAt = recordedAt;
    }
}
