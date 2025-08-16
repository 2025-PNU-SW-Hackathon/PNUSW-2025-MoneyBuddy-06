package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.response.CategoryRatioViewDto;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.BudgetAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class BudgetAnalyticsController {

    private final BudgetAnalyticsService budgetAnalyticsService;

    @GetMapping("/category-ratio")
    public CategoryRatioViewDto categoryRatio(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth ym
    ) {
        Long userId = resolveUserId();
        return budgetAnalyticsService.categoryRatio(userId, ym);
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
