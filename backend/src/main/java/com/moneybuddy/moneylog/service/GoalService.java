package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Goal;
import com.moneybuddy.moneylog.dto.response.GoalDto;
import com.moneybuddy.moneylog.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Transactional
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalDto upsert(Long userId, YearMonth ym, BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("amount는 0 이상이어야 합니다.");
        }
        var existing = goalRepository.findByUserIdAndYearAndMonth(userId, ym.getYear(), ym.getMonthValue())
                .orElse(null);

        if (existing == null) {
            existing = Goal.of(userId, ym, amount);
            goalRepository.save(existing);
        }
        else {
            existing.setAmount(amount);
        }
        return new GoalDto(ym.toString(), existing.getAmount());
    }

    public GoalDto get(Long userId, YearMonth ym) {
        var g = goalRepository.findByUserIdAndYearAndMonth(userId, ym.getYear(), ym.getMonthValue())
                .orElse(null);
        return (g == null) ? new GoalDto(ym.toString(), null)
                           : new GoalDto(ym.toString(), g.getAmount());
    }
}
