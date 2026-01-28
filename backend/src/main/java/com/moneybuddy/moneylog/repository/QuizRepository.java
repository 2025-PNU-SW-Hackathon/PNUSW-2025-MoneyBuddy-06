package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // 이미 푼 문제 제외하고 가져오기
    List<Quiz> findAllByIdNotIn(List<Long> ids);

    // 중복 문제 방지 (question 기준)
    boolean existsByQuestion(String question);
}
