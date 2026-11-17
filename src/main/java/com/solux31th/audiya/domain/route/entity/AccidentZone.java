package com.solux31th.audiya.domain.route.entity;

import com.solux31th.audiya.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 사고 다발 구역 공공데이터 (ERD accident_zone)
@Getter
@Entity
@Table(name = "accident_zone")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccidentZone extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "accident_zone_id")
    private Long id;

    @Column(name = "zone_type", length = 50)
    private String zoneType;

    // 공공데이터 원본 식별자
    @Column(name = "external_id", length = 50)
    private String externalId;

    @Column(name = "spot_name", length = 200)
    private String spotName;

    @Column(name = "region_name", length = 100)
    private String regionName;

    @Column(name = "occurrence_count")
    private Integer occurrenceCount;

    @Column(name = "casualty_count")
    private Integer casualtyCount;

    @Column(name = "death_count")
    private Integer deathCount;

    @Column(name = "center_lat", precision = 10, scale = 7)
    private BigDecimal centerLat;

    @Column(name = "center_lng", precision = 10, scale = 7)
    private BigDecimal centerLng;

    // TODO: boundary POLYGON(SRID 4326) 컬럼은 공간 타입 의존성(hibernate-spatial) 도입 여부를 팀과 정한 뒤 추가한다.
    //  그 전까지 구역 판정은 center_lat, center_lng 기준으로 처리한다.

    @Column(name = "base_year")
    private Integer baseYear;

    @Builder
    private AccidentZone(String zoneType, String externalId, String spotName, String regionName,
                         Integer occurrenceCount, Integer casualtyCount, Integer deathCount,
                         BigDecimal centerLat, BigDecimal centerLng, Integer baseYear) {
        this.zoneType = zoneType;
        this.externalId = externalId;
        this.spotName = spotName;
        this.regionName = regionName;
        this.occurrenceCount = occurrenceCount;
        this.casualtyCount = casualtyCount;
        this.deathCount = deathCount;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
        this.baseYear = baseYear;
    }
}
