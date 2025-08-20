package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.UserExp;
import com.moneybuddy.moneylog.dto.response.UserExpResponse;
import com.moneybuddy.moneylog.repository.UserExpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserExpService {

    private final UserExpRepository userExpRepository;

    public UserExpResponse getUserExp(Long userId) {
        UserExp userExp = userExpRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자의 경험치 정보를 찾을 수 없습니다."));

        return new UserExpResponse(userExp.getLevel(), userExp.getExperience());
    }
}
