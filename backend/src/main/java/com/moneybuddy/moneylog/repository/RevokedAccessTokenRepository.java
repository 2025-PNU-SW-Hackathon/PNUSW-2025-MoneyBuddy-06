package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.RevokedAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;

public interface RevokedAccessTokenRepository extends JpaRepository<RevokedAccessToken, Long> {
    boolean existsByJti(String jti);
    long deleteByExpiresAtBefore(LocalDateTime time);
}