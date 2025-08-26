package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "financial_title_cache")
@Getter
@NoArgsConstructor
public class FinancialTitleCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @Column(length = 300, nullable = false)
    private String title;

    public FinancialTitleCache(LocalDate date, String title) {
        this.date = date;
        this.title = title;
    }
}
