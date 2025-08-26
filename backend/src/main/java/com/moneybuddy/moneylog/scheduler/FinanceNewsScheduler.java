package com.moneybuddy.moneylog.scheduler;

import com.moneybuddy.moneylog.service.DailyFinanceNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class FinanceNewsScheduler {

    private final DailyFinanceNewsService service;

    // 매일 09:00 KST, 피드당 4개 후보 → 최종 8개 저장
    // 키워드당 1건, 제목 유사도 0.65 이상이면 중복으로 간주
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void run() {
        service.ingestTodayWithDedupe(
                4,   // perFeed
                8,   // topK
                1,   // maxPerKw
                0.65 // titleSim
        );
    }
}
