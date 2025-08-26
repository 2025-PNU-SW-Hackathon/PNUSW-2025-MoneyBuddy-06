package com.moneybuddy.moneylog.config;

import com.moneybuddy.moneylog.domain.FinancialKnowledge;
import com.moneybuddy.moneylog.repository.FinancialKnowledgeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class FinancialKnowledgeLoader {

    @Bean
    public CommandLineRunner initAugustCardNews(FinancialKnowledgeRepository repository) {
        return args -> {
            for (int day = 1; day <= 31; day++) {
                LocalDate date = LocalDate.of(2025, 8, day);
                if (repository.existsByDate(date)) {
                    continue; // 이미 존재하면 건너뜀
                }

                FinancialKnowledge news = new FinancialKnowledge(
                        "8월 카드뉴스 제목 " + day,
                        "8월 " + day + "일자 카드뉴스 본문입니다.",
                        date
                );
                repository.save(news);
            }
        };
    }
}
