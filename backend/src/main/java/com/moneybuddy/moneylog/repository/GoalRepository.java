package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {
    Optional<Goal> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);
}
