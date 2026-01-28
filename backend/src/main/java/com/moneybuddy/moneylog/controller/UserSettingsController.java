package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.request.ChangePasswordRequest;
import com.moneybuddy.moneylog.dto.request.PushSettingRequest;
import com.moneybuddy.moneylog.dto.response.PushSettingResponse;
import com.moneybuddy.moneylog.security.SecurityUtils;
import com.moneybuddy.moneylog.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserAccountService userAccountService;

    // 푸시 on/off 조회
    @GetMapping("/notifications/push")
    public ResponseEntity<PushSettingResponse> getPushSetting() {
        Long userId = SecurityUtils.currentUserId();
        boolean enabled = userAccountService.getPushEnabled(userId);
        return ResponseEntity.ok(new PushSettingResponse(enabled));
    }

    // 푸시 on/off 변경
    @PatchMapping("/notifications/push")
    public ResponseEntity<Void> updatePushSetting(@RequestBody PushSettingRequest req) {
        Long userId = SecurityUtils.currentUserId();
        userAccountService.updatePushEnabled(userId, req.enabled());
        return ResponseEntity.noContent().build();
    }
}
