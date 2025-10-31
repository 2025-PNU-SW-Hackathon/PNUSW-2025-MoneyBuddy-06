package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserExp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserExpRepository extends JpaRepository<UserExp, Long> {
    Optional<UserExp> findByUser_Id(Long userId); // 연관관계 기반 조회
    boolean existsByUser_Id(Long userId);
    void deleteByUserId(Long userId);
}

