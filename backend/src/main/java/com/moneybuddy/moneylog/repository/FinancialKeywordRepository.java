package com.moneybuddy.moneylog.repository;


import com.moneybuddy.moneylog.domain.FinancialKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface FinancialKeywordRepository extends JpaRepository<FinancialKeyword, Long> {
    int countByDateAndKeyword(LocalDate date, String keyword);
    boolean existsByDateAndKeyword(LocalDate date, String keyword);
}
