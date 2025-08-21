package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.ChangePasswordRequest;
import com.moneybuddy.moneylog.security.CustomUserDetails;
import com.moneybuddy.moneylog.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        Long userId = user.getUserId();
        userAccountService.changePassword(userId, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Jwt jwt) {
        Number uid = jwt.getClaim("userId");
        if (uid == null) throw new IllegalArgumentException("인증 토큰에 userId가 없습니다.");
        return uid.longValue();
    }
}
