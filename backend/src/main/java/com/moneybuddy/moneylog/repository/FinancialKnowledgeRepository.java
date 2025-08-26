package com.moneybuddy.moneylog.repository;


import com.moneybuddy.moneylog.domain.FinancialKeyword;
import com.moneybuddy.moneylog.domain.FinancialKnowledge;
import com.moneybuddy.moneylog.domain.FinancialTitleCache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialKnowledgeRepository extends JpaRepository<FinancialKnowledge, Long> {
    // 날짜 기준으로 오늘의 카드뉴스 조회
    List<FinancialKnowledge> findAllByDate(LocalDate date);
    boolean existsByDate(LocalDate date); // 오늘 날짜 데이터 있는지 확인용
    boolean existsByTitleAndDate(String title, LocalDate date);
}

