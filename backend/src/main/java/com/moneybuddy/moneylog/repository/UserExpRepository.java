package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserExp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserExpRepository extends JpaRepository<UserExp, Long> {
    Optional<UserExp> findByUserId(Long userId);
}

