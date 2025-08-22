package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.MobtiBriefDto;
import com.moneybuddy.moneylog.dto.MobtiFullDto;
import com.moneybuddy.moneylog.dto.MobtiResultDto;
import com.moneybuddy.moneylog.dto.MobtiSubmitRequest;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.MobtiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mobti")
@RequiredArgsConstructor
public class MobtiController {

    private final MobtiService mobtiService;

    @PostMapping("/submit")
    public MobtiResultDto submit(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody MobtiSubmitRequest request
    ) {
        if (principal == null) {
            throw new IllegalStateException("인증 정보가 없습니다. 로그인 후 시도하세요.");
        }
        Long userId = principal.getUserId();
        return mobtiService.calculateSaveAndReturn(userId, request);
    }

    // 요약(코드/닉네임/한줄요약)
    @GetMapping("/me/summary")
    public MobtiBriefDto mySummary(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("인증 정보가 없습니다. 로그인 후 시도하세요.");
        }
        return mobtiService.getMyMobtiBrief(principal.getUserId());
    }

    // 상세(코드/닉네임/한줄요약/상세설명)
    @GetMapping("/me/details")
    public MobtiFullDto myDetails(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("인증 정보가 없습니다. 로그인 후 시도하세요.");
        }
        return mobtiService.getMyMobtiFull(principal.getUserId());
    }
}
