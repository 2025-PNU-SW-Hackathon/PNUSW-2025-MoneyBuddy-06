package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserChallengeSuccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserChallengeSuccessRepository extends JpaRepository<UserChallengeSuccess, Long> {
    // 오늘 성공 여부 확인
    boolean existsByUserIdAndChallengeIdAndSuccessDate(Long userId, Long challengeId, LocalDate date);

    // 오늘 성공 기록 삭제
    void deleteByUserIdAndChallengeIdAndSuccessDate(Long userId, Long challengeId, LocalDate date);

    // 특정 기간 내 성공 횟수
    long countByUserIdAndChallengeIdAndSuccessDateBetween(
            Long userId, Long challengeId, LocalDate start, LocalDate end
    );

    // 지금까지 전체 성공 일 수
    int countByUserIdAndChallengeId(Long userId, Long challengeId);

    // 최근 성공 날짜 1개 (예: 통계 등)
    Optional<UserChallengeSuccess> findTopByUserIdAndChallengeIdOrderBySuccessDateDesc(Long userId, Long challengeId);

}
