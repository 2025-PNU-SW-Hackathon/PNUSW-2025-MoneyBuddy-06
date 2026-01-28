package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.domain.UserExp;
import com.moneybuddy.moneylog.dto.response.UserExpResponse;
import com.moneybuddy.moneylog.repository.UserExpRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserExpService {

    private final UserExpRepository userExpRepository;
    private final UserRepository userRepository;

    // 방어 로직: 없으면 생성하여 반환
    @Transactional
    public UserExpResponse getUserExp(Long userId) {
        // 1) PK로 먼저 조회
        UserExp userExp = userExpRepository.findById(userId)
                .orElseGet(() -> {
                    // 2) 없으면 User 존재 확인
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                    // 3) 생성 후 저장
                    UserExp created = UserExp.builder()
                            .user(user)       // @MapsId로 PK 공유
                            .experience(0)
                            .level(1)
                            .build();
                    return userExpRepository.save(created);
                });

        return new UserExpResponse(userExp.getLevel(), userExp.getExperience());
    }
}