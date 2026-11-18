package com.solux31th.audiya.domain.route.repository;

import com.solux31th.audiya.domain.route.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
}
