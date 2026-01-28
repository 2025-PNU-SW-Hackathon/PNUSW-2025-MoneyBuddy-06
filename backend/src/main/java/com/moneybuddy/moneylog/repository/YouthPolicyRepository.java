package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.YouthPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YouthPolicyRepository extends JpaRepository<YouthPolicy, Long> {
}
