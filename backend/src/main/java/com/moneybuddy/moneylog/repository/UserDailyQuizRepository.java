package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserDailyQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// 사용자별 오늘 문재 배정 + 풀이 기록
public interface UserDailyQuizRepository extends JpaRepository<UserDailyQuiz, Long> {

    // 아직 안 푼 배정(=completed=false) 1개 가져오기
    Optional<UserDailyQuiz> findFirstByUserIdAndCompletedFalse(Long userId);

    // 오늘 이미 푼 배정이 있는지 확인 (하루 1문제 제한)
    boolean existsByUserIdAndCompletedTrueAndAssignedAtBetween(
            Long userId, LocalDateTime start, LocalDateTime end
    );

    // 예전에 풀었던 문제는 빼고 배정
    @Query("select udq.quiz.id from UserDailyQuiz udq where udq.user.id = :userId and udq.completed = true")
    List<Long> findCompletedQuizIdsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}
