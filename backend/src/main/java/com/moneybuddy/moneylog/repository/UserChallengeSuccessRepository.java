package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserChallengeSuccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface UserChallengeSuccessRepository extends JpaRepository<UserChallengeSuccess, Long> {
    boolean existsByUserIdAndChallengeIdAndSuccessDate(Long userId, Long challengeId, LocalDate date);
    long countByUserIdAndChallengeId(Long userId, Long challengeId);

    long countByUserIdAndChallengeIdAndSuccessDateBetween(
            Long userId,
            Long challengeId,
            LocalDate start,
            LocalDate end
    );
}
