package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.model.NotificationAction;
import com.moneybuddy.moneylog.model.NotificationType;
import com.moneybuddy.moneylog.model.TargetType;
import com.moneybuddy.moneylog.port.Notifier;
import com.moneybuddy.moneylog.repository.GoalRepository;
import com.moneybuddy.moneylog.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;


// 월 목표 지출 대비 누적 지출이 80% 또는 100% 임계를 넘는 순간 알림을 보냄
// 100%를 넘는 경우가 80%도 함께 넘을 수 있으므로 100% 알림을 우선 발송
@Service
@RequiredArgsConstructor
public class BudgetWarningService {

    private static final BigDecimal T80 = new BigDecimal("0.8");
    private static final BigDecimal T100 = BigDecimal.ONE;

    private final GoalRepository goalRepository;
    private final LedgerRepository ledgerRepository;
    private final Notifier notifier;

    /**
     * @param userId
     * @param when
     * @param addedExpenseAbs
     */
    @Transactional(readOnly = true)
    public void checkAndNotifyOnExpense(Long userId, LocalDateTime when, BigDecimal addedExpenseAbs) {
        if (userId == null || addedExpenseAbs == null || addedExpenseAbs.signum() <= 0) return;

        YearMonth ym = (when != null ? YearMonth.from(when) : YearMonth.now());
        var goalOpt = goalRepository.findByUserIdAndYearAndMonth(userId, ym.getYear(), ym.getMonthValue());
        if (goalOpt.isEmpty()) return;

        BigDecimal limit = goalOpt.get().getAmount();
        if (limit == null || limit.signum() <= 0) return;

        var start = ym.atDay(1).atStartOfDay();
        var end = ym.plusMonths(1).atDay(1).atStartOfDay();

        // 현재 누적 지출(양수)
        BigDecimal spentNow = ledgerRepository.sumExpenseOnlyInRange(userId, start, end);

        // 저장 직전 누적 = 현재 - 이번 추가액
        BigDecimal spentBefore = spentNow.subtract(addedExpenseAbs);
        if (spentBefore.signum() < 0) spentBefore = BigDecimal.ZERO;

        BigDecimal rBefore = safeDiv(spentBefore, limit);
        BigDecimal rNow = safeDiv(spentNow, limit);

        boolean crossed80 = rBefore.compareTo(T80) < 0 && rNow.compareTo(T80) >= 0;
        boolean crossed100 = rBefore.compareTo(T100) < 0 && rNow.compareTo(T100) >= 0;

        if (crossed100) {
            send(userId, spentNow, limit, true);
        } else if (crossed80) {
            send(userId, spentNow, limit, false);
        }
    }

    private static BigDecimal safeDiv(BigDecimal a, BigDecimal b) {
        if (b == null || b.signum() == 0) return BigDecimal.ZERO;
        return a.divide(b, 4, RoundingMode.HALF_UP);   // 내부 계산용 4자리
    }

    private void send(Long userId, BigDecimal spend, BigDecimal limit, boolean over100) {
        String title = over100 ? "지출 목표 한도를 초과했어요" : "지출이 목표의 80%에 도달했어요";
        String body = over100 ? "이번 달 지출이 목표 금액을 넘었습니다."
                : "이번 달 지출이 목표의 80%를 넘었습니다.";

        Map<String, Object> params = Map.of(
                "spend", spend,
                "limit", limit,
                "ratio", safeDiv(spend, limit) // 예: 0.83, 1.02
        );

        notifier.send(
                userId,
                NotificationType.BUDGET_WARNING,
                TargetType.STATS,
                null,
                title,
                body,
                NotificationAction.OPEN_SPENDING_STATS,
                params,
                null
        );
    }
}
