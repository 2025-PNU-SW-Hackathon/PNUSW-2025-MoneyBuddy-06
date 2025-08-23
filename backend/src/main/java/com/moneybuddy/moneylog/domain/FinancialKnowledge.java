package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class FinancialKnowledge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    private Long id;

    private String title; // 카드뉴스 제목

    @Column(length = 3000) // 긴 본문도 수용 가능
    private String content;

    private LocalDate date; // 오늘의 카드뉴스 구분용 날짜

    public FinancialKnowledge(String title, String content, LocalDate date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }
}
