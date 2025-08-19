package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    boolean existsByUserIdAndChallengeId(Long userId, Long challengeId);
}
