package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.dto.UserStatusResponse;
import com.moneybuddy.moneylog.security.JwtUtil;
import com.moneybuddy.moneylog.repository.UserRepository;
import com.moneybuddy.moneylog.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserStatusResponse getUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserStatusResponse.builder()
                .userId(user.getId())
                .level(user.getLevel())
                .experience(user.getExperience())
                .build();
    }

}
