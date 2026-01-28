package com.moneybuddy.moneylog.scheduler;

import com.moneybuddy.moneylog.service.DailyFinancialCardNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class FinancialCardNewsScheduler {

    private final DailyFinancialCardNewsService service;

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

    // 서버 시작 시 즉시 실행하는 메서드 (테스트용)
    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        // 실제 스케줄러의 run() 로직을 그대로 호출
        int savedCount = service.ingestTodayWithDedupe(4, 8, 1, 0.65);
        System.out.println(">>> [STARTUP] 카드뉴스 즉시 수집 완료: " + savedCount + "개");

        // 주의: 배포 시에는 이 @EventListener 메서드를 주석 처리하거나 제거해야 합니다.
    }
}
