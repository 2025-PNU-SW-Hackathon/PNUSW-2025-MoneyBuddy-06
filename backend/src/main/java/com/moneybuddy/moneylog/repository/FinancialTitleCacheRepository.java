package com.moneybuddy.moneylog.repository;


import com.moneybuddy.moneylog.domain.FinancialTitleCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FinancialTitleCacheRepository extends JpaRepository<FinancialTitleCache, Long> {
    List<FinancialTitleCache> findAllByDate(LocalDate date);
}
