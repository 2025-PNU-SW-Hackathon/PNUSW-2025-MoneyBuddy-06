package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.domain.UserDeviceToken;
import com.moneybuddy.moneylog.repository.UserDeviceTokenRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserRepository userRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호가 기존 비밀번호와 동일합니다.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")));

        // 모든 디바이스 재인증 필요 (다시 로그인하기 전까지 일반 푸시 차단)
        for (UserDeviceToken t : userDeviceTokenRepository.findByUserId(userId)) {
            t.setReauthRequired(true);
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(user);
    }

    // 푸시 on/off
    @Transactional(readOnly = true)
    public boolean getPushEnabled(Long userId) {
        return userRepository.findById(userId).orElseThrow().isNotificationEnabled();
    }

    @Transactional
    public void updatePushEnabled(Long userId, boolean enabled) {
        User u = userRepository.findById(userId).orElseThrow();
        u.setNotificationEnabled(enabled); // DB에 저장 → 로그인/재로그인과 무관하게 유지
    }
}