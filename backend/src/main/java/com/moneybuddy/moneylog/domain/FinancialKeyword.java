package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

// 날짜+키워드 유니크(중복 키워드 방지용)
@Entity
@Table(
        name = "financial_keyword",
        uniqueConstraints = @UniqueConstraint(columnNames = {"date", "keyword"})
)
public class FinancialKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private String keyword; // 소문자 + 정규화된 표기

    protected FinancialKeyword() {}
    public FinancialKeyword(LocalDate date, String keyword) {
        this.date = date;
        this.keyword = keyword;
    }
}
