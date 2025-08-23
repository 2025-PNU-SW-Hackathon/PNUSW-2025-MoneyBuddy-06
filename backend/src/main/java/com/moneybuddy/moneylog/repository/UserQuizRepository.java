package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserQuiz;
import com.moneybuddy.moneylog.domain.Quiz;
import com.moneybuddy.moneylog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserQuizRepository extends JpaRepository<UserQuiz, Long> {
    Optional<UserQuiz> findByUserAndQuiz(User user, Quiz quiz);
}
