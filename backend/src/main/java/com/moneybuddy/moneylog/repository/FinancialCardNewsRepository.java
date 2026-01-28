package com.moneybuddy.moneylog.repository;


import com.moneybuddy.moneylog.domain.FinancialCardNews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FinancialCardNewsRepository extends JpaRepository<FinancialCardNews, Long> {
    // 날짜 기준으로 오늘의 카드뉴스 조회
    List<FinancialCardNews> findAllByDate(LocalDate date);
    boolean existsByDate(LocalDate date); // 오늘 날짜 데이터 있는지 확인용
    boolean existsByTitleAndDate(String title, LocalDate date);
}

