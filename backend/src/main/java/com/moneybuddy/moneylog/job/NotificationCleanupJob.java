package com.moneybuddy.moneylog.job;

import com.moneybuddy.moneylog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class NotificationCleanupJob {

    private final NotificationService notificationService;

    // 매일 04:00에 만료된 알림 삭제
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanup() {
        notificationService.cleanupExpired();
    }
}
