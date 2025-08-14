package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "goal",
        uniqueConstraints = @UniqueConstraint(name = "uk_goal_user_year_month", columnNames = {"userId", "year", "month"}),
        indexes = @Index(name = "idx_goal_user_year_month", columnList = "userId, year, month")
)
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;  // 목표 소비 금액, 0 허용

    public YearMonth toYearMonth() { return YearMonth.of(year, month); }

    public static Goal of(Long userId, YearMonth ym, BigDecimal amount) {
        return Goal.builder()
                .userId(userId)
                .year(ym.getYear())
                .month(ym.getMonthValue())
                .amount(amount)
                .build();
    }
}
