package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    boolean existsByUserIdAndChallengeId(Long userId, Long challengeId);
    // 진행 중 (completed = false)
    List<UserChallenge> findByUserIdAndCompletedFalse(Long userId);
    // 진행 완료 (completed = true)
    List<UserChallenge> findByUserIdAndCompletedTrue(Long userId);

    Optional<UserChallenge> findByUserIdAndChallengeId(Long userId, Long challengeId);
}
