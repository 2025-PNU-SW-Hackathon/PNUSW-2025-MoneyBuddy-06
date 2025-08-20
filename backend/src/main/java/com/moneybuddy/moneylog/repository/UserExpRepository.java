package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.UserExp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExpRepository extends JpaRepository<UserExp, Long> {
}
