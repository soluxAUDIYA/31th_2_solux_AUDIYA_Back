package com.solux31th.audiya.domain.trip.repository;

import com.solux31th.audiya.domain.trip.entity.TripReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripReportRepository extends JpaRepository<TripReport, Long> {
}
