package com.solux31th.audiya.domain.auth.repository;

import com.solux31th.audiya.domain.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByPhoneNumber(String phoneNumber);

    // 탈퇴하지 않은 계정을 전화번호로 조회
    Optional<User> findByPhoneNumberAndDeletedAtIsNull(String phoneNumber);
}