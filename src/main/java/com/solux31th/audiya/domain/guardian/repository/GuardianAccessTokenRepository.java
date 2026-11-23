package com.solux31th.audiya.domain.guardian.repository;
import com.solux31th.audiya.domain.guardian.entity.GuardianAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface GuardianAccessTokenRepository extends JpaRepository<GuardianAccessToken, Long> {
    Optional<GuardianAccessToken> findByTokenHash(String tokenHash);
}
