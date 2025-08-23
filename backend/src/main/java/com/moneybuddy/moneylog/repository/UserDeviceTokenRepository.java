package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    List<UserDeviceToken> findAllByUserId(Long userId);
    Optional<UserDeviceToken> findByUserIdAndDeviceToken(Long userId, String deviceToken);
    void deleteByUserIdAndDeviceToken(Long userId, String deviceToken);

    Optional<UserDeviceToken> findByDeviceToken(String deviceToken);
    List<UserDeviceToken> findByUserIdAndEnabledTrueAndReauthRequiredFalse(Long userId);
    List<UserDeviceToken> findByUserId(Long userId);
}