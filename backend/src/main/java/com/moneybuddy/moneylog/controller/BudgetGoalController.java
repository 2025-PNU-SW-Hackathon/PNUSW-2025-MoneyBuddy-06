package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.request.GoalUpsertRequest;
import com.moneybuddy.moneylog.dto.response.GoalDto;
import com.moneybuddy.moneylog.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.time.ZoneId;

@RestController
@RequestMapping("/budget-goal")
@RequiredArgsConstructor
public class BudgetGoalController {

    private final GoalService goalService;

    @PutMapping
    public GoalDto upsert(@RequestParam(required = false)
                          @DateTimeFormat(pattern = "yyyy-MM") YearMonth ym,
                          @RequestBody GoalUpsertRequest req) {
        Long userId = resolveUserId();
        YearMonth target = (ym != null) ? ym : YearMonth.now(ZoneId.of("Asia/Seoul"));
        return goalService.upsert(userId, target, req.getAmount());
    }

    // 목표 금액 조회 (목표 금액 설정 X -> 프론트에서 "목표를 설정해 주세요" 표시)
    @GetMapping
    public GoalDto get(@RequestParam(required = false)
                       @DateTimeFormat(pattern = "yyyy-MM") YearMonth ym) {
        Long userId = resolveUserId();
        YearMonth target = (ym != null) ? ym : YearMonth.now(ZoneId.of("Asia/Seoul"));
        return goalService.get(userId, target);
    }

    private Long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object p = auth.getPrincipal();
            if (p instanceof java.util.Map<?,?> m && m.get("userId") != null) {
                Object v = m.get("userId");
                if (v instanceof Long l) return l;
                if (v instanceof Integer i) return i.longValue();
                return Long.parseLong(v.toString());
            }
        }
        throw new AccessDeniedException("로그인이 필요합니다.");
    }
}
