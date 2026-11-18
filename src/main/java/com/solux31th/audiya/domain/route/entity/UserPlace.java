package com.solux31th.audiya.domain.route.entity;

import com.solux31th.audiya.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 사용자가 저장한 장소 (ERD user_place). 집, 회사 등
@Getter
@Entity
@Table(name = "user_place")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPlace extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_place_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type", nullable = false, length = 20)
    private PlaceType placeType;

    @Column(name = "place_name", nullable = false, length = 100)
    private String placeName;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private UserPlace(Long userId, PlaceType placeType, String placeName, String address,
                      BigDecimal latitude, BigDecimal longitude) {
        this.userId = userId;
        this.placeType = placeType;
        this.placeName = placeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
