package com.solux31th.audiya.domain.trip.repository;

import com.solux31th.audiya.domain.trip.entity.Trip;
import com.solux31th.audiya.domain.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    boolean existsByUserIdAndStatusInAndDeletedAtIsNull(Long userId, Collection<TripStatus> statuses);

    Optional<Trip> findFirstByUserIdAndStatusInAndDeletedAtIsNullOrderByCreatedAtDesc(
            Long userId, Collection<TripStatus> statuses);
}
