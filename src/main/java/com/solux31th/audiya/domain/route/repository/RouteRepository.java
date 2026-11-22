package com.solux31th.audiya.domain.route.repository;

import com.solux31th.audiya.domain.route.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {

    // 귀가에서 사용자가 선택한 경로 (귀가당 최대 1개)
    Optional<Route> findByTripIdAndSelectedTrue(Long tripId);
}
