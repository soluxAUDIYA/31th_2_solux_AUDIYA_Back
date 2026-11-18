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
import java.time.LocalDate;
import java.time.LocalDateTime;

// 안전 시설물 공공데이터 (ERD safety_facility). CCTV, 가로등
@Getter
@Entity
@Table(name = "safety_facility")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SafetyFacility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "safety_facility_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "facility_type", nullable = false, length = 20)
    private FacilityType facilityType;

    @Column(name = "install_purpose", length = 100)
    private String installPurpose;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    // TODO: location POINT(SRID 4326) 컬럼은 공간 타입 의존성(hibernate-spatial) 도입 여부를 팀과 정한 뒤 추가한다.
    //  그 전까지 거리 계산은 latitude, longitude로 처리한다.

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(length = 100)
    private String source;

    @Column(name = "data_date")
    private LocalDate dataDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private SafetyFacility(FacilityType facilityType, String installPurpose, String address,
                           BigDecimal latitude, BigDecimal longitude, String regionCode,
                           String source, LocalDate dataDate) {
        this.facilityType = facilityType;
        this.installPurpose = installPurpose;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.regionCode = regionCode;
        this.source = source;
        this.dataDate = dataDate;
    }
}
