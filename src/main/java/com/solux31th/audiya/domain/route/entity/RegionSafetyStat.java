package com.solux31th.audiya.domain.route.entity;

import com.solux31th.audiya.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// 지역별 안전 시설 통계 (ERD region_safety_stat). 지역 코드 + 기준 연도당 하나
@Getter
@Entity
@Table(
        name = "region_safety_stat",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_region_safety_stat_region_code_base_year",
                columnNames = {"region_code", "base_year"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionSafetyStat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_safety_stat_id")
    private Long id;

    @Column(name = "region_code", nullable = false, length = 10)
    private String regionCode;

    @Column(name = "region_name", length = 100)
    private String regionName;

    @Column(name = "cctv_count")
    private Integer cctvCount;

    @Column(name = "light_count")
    private Integer lightCount;

    @Column(precision = 10, scale = 3)
    private BigDecimal area;

    @Column(name = "correction_factor", precision = 5, scale = 3)
    private BigDecimal correctionFactor;

    @Column(name = "base_year", nullable = false)
    private Integer baseYear;

    @Builder
    private RegionSafetyStat(String regionCode, String regionName, Integer cctvCount, Integer lightCount,
                             BigDecimal area, BigDecimal correctionFactor, Integer baseYear) {
        this.regionCode = regionCode;
        this.regionName = regionName;
        this.cctvCount = cctvCount;
        this.lightCount = lightCount;
        this.area = area;
        this.correctionFactor = correctionFactor;
        this.baseYear = baseYear;
    }
}
