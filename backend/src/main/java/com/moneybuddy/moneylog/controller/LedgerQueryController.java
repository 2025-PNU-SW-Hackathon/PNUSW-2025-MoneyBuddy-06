package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.response.DaySummaryDto;
import com.moneybuddy.moneylog.dto.response.MonthSummaryDto;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.LedgerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerQueryController {

    private final LedgerQueryService ledgerQueryService;

    // 한 달 보기 (2025-08)
    @GetMapping("/month")
    public MonthSummaryDto month(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth ym
    ) {
        Long userId = resolveUserId();
        return ledgerQueryService.getMonth(userId, ym);
    }

    // 하루 보기 (2025-08-14)
    @GetMapping("/day")
    public DaySummaryDto day(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long userId = resolveUserId();
        return ledgerQueryService.getDay(userId, date);
    }

    private Long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUserId();
            }
        }
        throw new AccessDeniedException("로그인이 필요합니다.");
    }
}
