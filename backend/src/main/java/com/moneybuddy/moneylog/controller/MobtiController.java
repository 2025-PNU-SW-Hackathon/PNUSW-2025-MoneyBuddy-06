package com.moneybuddy.moneylog.controller;

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
}
