package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.DeviceTokenRequest;
import com.moneybuddy.moneylog.domain.UserDeviceToken;
import com.moneybuddy.moneylog.repository.UserDeviceTokenRepository;
import com.moneybuddy.moneylog.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final UserDeviceTokenRepository repository;

    // 로그인 직후, 앱 실행 시 갱신 등
    @PostMapping
    public void register(@Valid @RequestBody DeviceTokenRequest req) {
        Long userId = SecurityUtils.currentUserId();
        repository.findByUserIdAndDeviceToken(userId, req.getDeviceToken())
                .orElseGet(() -> {
                    UserDeviceToken t = new UserDeviceToken();
                    t.setUserId(userId);
                    t.setDeviceToken(req.getDeviceToken());
                    return repository.save(t);
                });
    }

    // 로그아웃/앱 제거 전
    @DeleteMapping
    public void unregister(@RequestParam String deviceToken) {
        Long userId = SecurityUtils.currentUserId();
        repository.deleteByUserIdAndDeviceToken(userId, deviceToken);
    }
}

