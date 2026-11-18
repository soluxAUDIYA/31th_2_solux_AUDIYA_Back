package com.solux31th.audiya.domain.trip.repository;

import com.solux31th.audiya.domain.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
}
