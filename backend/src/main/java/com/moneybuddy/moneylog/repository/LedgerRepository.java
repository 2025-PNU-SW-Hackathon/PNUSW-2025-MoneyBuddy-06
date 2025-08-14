package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.Ledger;
import com.moneybuddy.moneylog.dto.response.SumResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
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
        select coalesce(sum(case when l.amount < 0 then -l.amount else 0 end), 0)
        from Ledger l
        where l.userId = :userId
          and l.dateTime >= :start
          and l.dateTime < :end
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
}
