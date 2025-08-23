package com.moneybuddy.moneylog.finance;

import com.moneybuddy.moneylog.domain.FinancialKnowledge;
import com.moneybuddy.moneylog.model.NotificationAction;
import com.moneybuddy.moneylog.model.NotificationType;
import com.moneybuddy.moneylog.model.TargetType;
import com.moneybuddy.moneylog.port.Notifier;
import com.moneybuddy.moneylog.repository.FinancialKnowledgeRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class FinanceInfoScheduler {

    private final UserRepository userRepository;
    private final FinancialKnowledgeRepository knowledgeRepository;
    private final Notifier notifier;

    // 매일 10:00에 "오늘의 금융 정보" 카드뉴스 알림 발송
    @Scheduled(cron = "0 0 10 * * *")
    public void pushDailyFinanceInfo() {
        LocalDate today = LocalDate.now();

        // 오늘자 카드뉴스가 없으면 알림 X
        List<FinancialKnowledge> todayNews = knowledgeRepository.findAllByDate(today);
        if (todayNews.isEmpty()) return;

        // 딥링크 파라미터 (날짜 기준 목록 화면으로 진입)
        Map<String, Object> params = Map.of(
                "date", today.toString()
        );

        var userIds = userRepository.findAll().stream().map(u -> u.getId()).toList();

        for (Long uid : userIds) {
            notifier.send(
                    uid,
                    NotificationType.FINANCE_INFO,
                    TargetType.ARTICLE,
                    null,
                    "오늘의 금융 카드뉴스",
                    "하루 한 장 금융 상식! 지금 확인하세요.",
                    NotificationAction.OPEN_FINANCE_ARTICLE,
                    params,
                    null
            );
        }
    }
}
