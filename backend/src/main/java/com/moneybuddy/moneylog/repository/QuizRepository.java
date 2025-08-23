package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByQuizDate(LocalDate quizDate);
}
