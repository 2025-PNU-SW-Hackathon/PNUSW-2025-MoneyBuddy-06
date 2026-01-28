package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.Ledger;
import com.moneybuddy.moneylog.dto.response.SumResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {

    List<Ledger> findAllByUserIdAndDateTimeBetween(
            Long userId, LocalDateTime start, LocalDateTime end, Sort sort
    );

    @Query("""
            select new com.moneybuddy.moneylog.dto.response.SumResult(
                coalesce(sum(case when l.amount < 0 then -l.amount else 0 end), 0),
                coalesce(sum(case when l.amount > 0 then l.amount else 0 end), 0)
            )
            from Ledger l
            where l.userId = :userId
                and l.dateTime >= :start
                and l.dateTime < :end
    """)
    SumResult sumExpenseIncomeInRange(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT COALESCE(SUM(-l.amount), 0)
            FROM Ledger l
            WHERE l.userId = :userId
                AND l.entryType = com.moneybuddy.moneylog.domain.EntryType.EXPENSE
                AND l.dateTime >= :start AND l.dateTime < :end
    """)
    BigDecimal sumExpenseOnlyInRange(Long userId, LocalDateTime start, LocalDateTime end);


    // 카테고리별 지출 합(양수)
    @Query("""
        select l.category as category,
               coalesce(sum(case when l.amount < 0 then -l.amount else 0 end), 0) as expense
        from Ledger l
        where l.userId = :userId
          and l.dateTime >= :start
          and l.dateTime < :end
        group by l.category
        having coalesce(sum(case when l.amount < 0 then -l.amount else 0 end), 0) > 0
        order by expense desc
    """)
    List<CategoryExpenseRow> sumExpenseByCategoryInRange(Long userId, LocalDateTime start, LocalDateTime end);

    interface CategoryExpenseRow {
        String getCategory();
        BigDecimal getExpense();
    }

    // 일자별 집계(무지출/일일예산 판단용)
    @Query("""
            select function('date', l.dateTime) as day,
                   coalesce(sum(case when l.amount < 0 then -l.amount else 0 end), 0) as expense,
                   coalesce(sum(case when l.amount > 0 then l.amount else 0 end), 0) as income
            from Ledger l
            where l.userId = :userId
              and l.dateTime >= :start
              and l.dateTime < :end
            group by function('date', l.dateTime)
            order by day
    """)
    List<DailyAggRow> dailyAggBetween(Long userId, LocalDateTime start, LocalDateTime end);
    interface DailyAggRow {
        LocalDate getDay();
        BigDecimal getExpense();
        BigDecimal getIncome();
    }

    // 특정 카테고리 지출 합
    @Query("""
            select coalesce(sum(case when l.amount < 0 then -l.amount else 0 end), 0)
            from Ledger l
            where l.userId = :userId
              and l.dateTime >= :start
              and l.dateTime < :end
              and l.category = :category
    """)
    BigDecimal sumExpenseOfCategoryInRange(Long userId, LocalDateTime start, LocalDateTime end, String category);

    // "건당 n원 이하" 거리 횟수(카테고리 한정)
    @Query("""
            select count(l)
            from Ledger l
            where l.userId = :userId
                and l.dateTime >= :start
                and l.dateTime < :end
                and l.category = :category
                and l.amount < 0
                and (-l.amount) <= :maxAmount
    """)
    long countExpenseTransactionsUnderInCategory(Long userId, LocalDateTime start, LocalDateTime end,
                                                 String category, BigDecimal maxAmount);

    // 가장 많이 쓰는 카테고리(건수 기준)
    @Query("""
            select l.category as category, count(l) as cnt
            from Ledger l
            where l.userId = :userId
              and l.amount < 0
              and l.dateTime >= :start
              and l.dateTime < :end
            group by l.category
            order by cnt desc
    """)
    List<CategoryCountRow> topExpenseCategoriesByCount(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    interface CategoryCountRow {
        String getCategory();
        Long getCnt();
    }
}
