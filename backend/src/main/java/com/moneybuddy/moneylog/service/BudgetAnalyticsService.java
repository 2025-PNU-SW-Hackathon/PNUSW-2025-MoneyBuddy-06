package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.dto.CategoryRatioItemDto;
import com.moneybuddy.moneylog.dto.CategoryRatioViewDto;
import com.moneybuddy.moneylog.repository.GoalRepository;
import com.moneybuddy.moneylog.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetAnalyticsService {

    private final LedgerRepository ledgerRepository;
    private final GoalRepository goalRepository;

    public CategoryRatioViewDto categoryRatio(Long userId, YearMonth ym) {
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        // 최신 목표 로드
        BigDecimal goal = goalRepository.findByUserIdAndYearAndMonth(userId, ym.getYear(), ym.getMonthValue())
                .map(g -> g.getAmount())
                .orElse(null);

        // 이 달 지출 합(양수)
        BigDecimal spent = ledgerRepository.sumExpenseOnlyInRange(userId, start, end);

        // 카테고리별 지출(양수)
        var rows = ledgerRepository.sumExpenseByCategoryInRange(userId, start, end);

        // 기준 선택: 목표가 있고(goal>0) 미만/이상에 따라 분모 결정
        boolean hasPositiveGoal = (goal != null && goal.signum() > 0);
        boolean exceeded = hasPositiveGoal && spent.compareTo(goal) > 0;

        BigDecimal baseline;
        String baselineName;
        if (hasPositiveGoal && !exceeded) {
            baseline = goal;
            baselineName = "GOAL";
        } else {
            // 목표가 없거나 초과한 경우 -> 실제 지출 합을 분모
            baseline = spent;
            baselineName = "SPENT";
        }

        // 분모가 0이면 모든 비율 0 처리
        List<CategoryRatioItemDto> items = new ArrayList<>();
        for (var r : rows) {
            BigDecimal expense = r.getExpense();
            BigDecimal ratio = (baseline.signum() == 0)
                    ? BigDecimal.ZERO
                    : expense.multiply(BigDecimal.valueOf(100))
                    .divide(baseline, 4, RoundingMode.HALF_UP) // 내부 계산 4자리
                    .setScale(2, RoundingMode.HALF_UP);       // 응답은 소수점 2자리
            items.add(new CategoryRatioItemDto(r.getCategory(), expense, ratio));
        }

        return new CategoryRatioViewDto(
                ym.toString(),
                goal,
                spent,
                exceeded,
                baselineName,
                items
        );
    }
}
