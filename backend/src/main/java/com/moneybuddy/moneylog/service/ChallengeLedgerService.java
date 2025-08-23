package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.dto.response.SumResult;
import com.moneybuddy.moneylog.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChallengeLedgerService {

    private final LedgerRepository repo;

    private static LocalDateTime startOf(LocalDate d) {
        return d.atStartOfDay();
    }
    private static LocalDateTime endOf(LocalDate d) {
        return d.plusDays(1).atStartOfDay();
    }

    private static LocalDateTime[] range(LocalDate start, LocalDate endInclusive) {
        return  new LocalDateTime[]{ startOf(start), endOf(endInclusive) };
    }

    // M-1, S-1: 수입 대비 저축률 계산 (저축은 카테고리 "저축" 합으로 가정)
    public BigDecimal savingsRate(LocalDate start, LocalDate endInclusive, Long userId) {
        var r = range(start, endInclusive);
        SumResult sum = repo.sumExpenseIncomeInRange(userId, r[0], r[1]);
        BigDecimal income = sum.income();
        if (income.signum() == 0) return BigDecimal.ZERO;

        BigDecimal savings = repo.sumExpenseOfCategoryInRange(userId, r[0], r[1], "저축");
        return savings.divide(income, 4, RoundingMode.HALF_UP);
    }

    // M-2: 무지출 데이 개수
    public long countNoSpendDays(LocalDate start, LocalDate endInclusive, Long userId) {
        var r = range(start,endInclusive);
        var rows = repo.dailyAggBetween(userId, r[0], r[1]);
        long days = endInclusive.toEpochDay() - start.toEpochDay() + 1;

        var map = new java.util.HashMap<LocalDate, BigDecimal>();
        for (var row : rows)
            map.put(row.getDay(), row.getExpense());

        long cnt = 0;
        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);
            BigDecimal exp = map.getOrDefault(d, BigDecimal.ZERO);
            if (exp.signum() == 0) cnt++;
        }
        return cnt;
    }

    // S-5: 일일 지출 n원 이하인 날 개수
    public long countDaysUnderDailyBudget(LocalDate start, LocalDate endInclusive, Long userId, BigDecimal won) {
        var r = range(start, endInclusive);
        var rows = repo.dailyAggBetween(userId, r[0], r[1]);
        long days = endInclusive.toEpochDay() - start.toEpochDay() + 1;

        var map = new java.util.HashMap<LocalDate, BigDecimal>();
        for (var row : rows) map.put(row.getDay(), row.getExpense());

        long cnt = 0;
        for (int i=0;i<days;i++) {
            LocalDate d = start.plusDays(i);
            BigDecimal exp = map.getOrDefault(d, BigDecimal.ZERO);
            if (exp.compareTo(won) <= 0) cnt++;
        }
        return cnt;
    }

    // I-3: 지난 기간(예: 직전 4주) 동안 가장 많이 쓴 카테고리 한 개
    public String mostUsedCategory(LocalDate lookbackStart, LocalDate lookbackEndInclusive, Long userId) {
        var r = range(lookbackStart, lookbackEndInclusive);
        var top = repo.topExpenseCategoriesByCount(userId, r[0], r[1], PageRequest.of(0, 1));
        return top.isEmpty() ? null : top.get(0).getCategory();
    }

    // I-3: 주어진 카테고리에서 "건당 maxWon 이하" 결제 건수
    public long countSmallPaymentsInCategory(LocalDate start, LocalDate endInclusive, Long userId,
                                             String category, BigDecimal maxWon) {
        var r = range(start, endInclusive);
        return repo.countExpenseTransactionsUnderInCategory(userId, r[0], r[1], category, maxWon);
    }

    // S-3: 특정 카테고리(식비) 지출 합
    public BigDecimal sumExpenseOfCategory(LocalDate start, LocalDate endInclusive, Long userId, String category) {
        var r = range(start, endInclusive);
        return repo.sumExpenseOfCategoryInRange(userId, r[0], r[1], category);
    }
}
