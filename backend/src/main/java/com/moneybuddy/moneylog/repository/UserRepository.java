package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 모든 사용자 ID만 가져오기
    @Query("select u.id from User u")
    List<Long> findAllIds();
  
    Optional<User> findByEmail(String email);
}