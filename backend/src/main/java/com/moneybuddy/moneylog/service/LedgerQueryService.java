package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Ledger;
import com.moneybuddy.moneylog.dto.DaySummaryDto;
import com.moneybuddy.moneylog.dto.LedgerEntryDto;
import com.moneybuddy.moneylog.dto.MonthSummaryDto;
import com.moneybuddy.moneylog.dto.response.*;
import com.moneybuddy.moneylog.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerQueryService {

    private final LedgerRepository ledgerRepository;

    public MonthSummaryDto getMonth(Long userId, YearMonth ym) {
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay();

        SumResult sum = ledgerRepository.sumExpenseIncomeInRange(userId, start, end);
        BigDecimal balance = sum.income().subtract(sum.expense());

        List<LedgerEntryDto> entries = ledgerRepository
                .findAllByUserIdAndDateTimeBetween(
                        userId, start, end, Sort.by(Sort.Direction.ASC, "dateTime"))
                .stream()
                .map(LedgerQueryService::toDto)
                .toList();

        return new MonthSummaryDto(ym.toString(), sum.expense(), sum.income(), balance, entries);
    }

    public DaySummaryDto getDay(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        SumResult sum = ledgerRepository.sumExpenseIncomeInRange(userId, start, end);

        List<LedgerEntryDto> entries = ledgerRepository
                .findAllByUserIdAndDateTimeBetween(
                        userId, start, end, Sort.by(Sort.Direction.ASC, "dateTime"))
                .stream()
                .map(LedgerQueryService::toDto)
                .toList();

        return new DaySummaryDto(date.toString(), sum.expense(), sum.income(), entries);
    }

    private static LedgerEntryDto toDto(Ledger l) {
        return new LedgerEntryDto(l);
    }
}
