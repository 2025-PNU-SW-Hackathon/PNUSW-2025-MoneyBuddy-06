package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.request.ChangePasswordRequest;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.security.SecurityUtils;
import com.moneybuddy.moneylog.service.UserAccountService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    // 비밀번호 변경 (변경 직후 기존 토큰 전부 401 처리)
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        Long userId = SecurityUtils.currentUserId();
        userAccountService.changePassword(userId, req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("userId");
        if (uid == null) throw new IllegalArgumentException("인증 토큰에 userId가 없습니다.");
        return uid.longValue();
    }
}