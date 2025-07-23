package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.dto.UserProfileDto;
import com.moneybuddy.moneylog.dto.MobtiRequestDto;
import com.moneybuddy.moneylog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MobtiService {
    private final UserRepository userRepository;

    public MobtiService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserProfileDto saveResult(Long userId, MobtiRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        user.setMobti(dto.getMobti());
        userRepository.save(user);

        return new UserProfileDto(
                user.getId(),
                user.getEmail(),
                user.getMobti()
        );
    }
}
