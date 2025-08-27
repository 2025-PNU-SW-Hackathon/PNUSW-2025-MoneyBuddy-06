package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {

    boolean existsByUserIdAndChallengeId(Long userId, Long challengeId);
    int countByChallengeId(Long challengeId);

    // 진행 중 (completed = false)
    List<UserChallenge> findByUserIdAndCompletedFalse(Long userId);
    // 진행 완료 (completed = true)
    List<UserChallenge> findByUserIdAndCompletedTrue(Long userId);

    Optional<UserChallenge> findByUserIdAndChallengeId(Long userId, Long challengeId);

    @Query("SELECT uc FROM UserChallenge uc WHERE uc.completed = false")
    List<UserChallenge> findAllIncomplete();

    @Query("SELECT uc FROM UserChallenge uc JOIN FETCH uc.challenge WHERE uc.userId = :userId AND uc.completed = false")
    List<UserChallenge> findOngoingWithChallenge(@Param("userId") Long userId);
}
