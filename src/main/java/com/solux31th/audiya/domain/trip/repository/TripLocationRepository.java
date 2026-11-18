package com.solux31th.audiya.domain.trip.repository;

import com.solux31th.audiya.domain.trip.entity.TripLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripLocationRepository extends JpaRepository<TripLocation, Long> {
}
