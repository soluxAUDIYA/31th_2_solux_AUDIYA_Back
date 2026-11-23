package com.solux31th.audiya.domain.trip.entity;

import com.solux31th.audiya.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 귀가 세션 (ERD trip). user는 다른 도메인 엔티티이므로 연관관계 대신 ID만 보관한다.
@Getter
@Entity
@Table(
        name = "trip",
        indexes = @Index(name = "idx_trip_user_id_status", columnList = "user_id, status"),
        uniqueConstraints = @UniqueConstraint(name = Trip.ACTIVE_USER_UNIQUE_KEY, columnNames = "active_user_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trip extends BaseTimeEntity {

    public static final String ACTIVE_USER_UNIQUE_KEY = "uk_trip_active_user_id";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "origin_name", length = 100)
    private String originName;

    @Column(name = "origin_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal originLat;

    @Column(name = "origin_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal originLng;

    @Column(name = "destination_name", length = 100)
    private String destinationName;

    @Column(name = "destination_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal destinationLat;

    @Column(name = "destination_lng", nullable = false, precision = 10, scale = 7)
    private BigDecimal destinationLng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 진행 중(READY/IN_PROGRESS)이고 삭제되지 않은 귀가만 user_id 값을 갖고, 나머지는 NULL인 계산 컬럼.
    // UNIQUE 제약(uk_trip_active_user_id)으로 사용자당 진행 중 귀가 1건을 DB에서 보장한다. (MySQL은 NULL 중복을 허용)
    @Getter(AccessLevel.NONE)
    @Column(name = "active_user_id", insertable = false, updatable = false,
            columnDefinition = "bigint GENERATED ALWAYS AS (CASE WHEN status IN ('READY', 'IN_PROGRESS') "
                    + "AND deleted_at IS NULL THEN user_id END) STORED")
    private Long activeUserId;

    @Builder
    private Trip(Long userId, String originName, BigDecimal originLat, BigDecimal originLng,
                 String destinationName, BigDecimal destinationLat, BigDecimal destinationLng) {
        this.userId = userId;
        this.originName = originName;
        this.originLat = originLat;
        this.originLng = originLng;
        this.destinationName = destinationName;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
        this.status = TripStatus.READY;
    }
}
