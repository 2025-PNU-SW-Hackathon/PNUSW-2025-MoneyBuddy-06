package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>{
}
