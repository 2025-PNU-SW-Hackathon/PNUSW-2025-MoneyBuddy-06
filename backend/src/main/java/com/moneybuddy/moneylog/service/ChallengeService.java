package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.dto.ChallengeResponse;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public List<ChallengeResponse> getRecommendedChallenges(Long userId) {
        String mobti = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."))
                .getMobti();

        List<String> mobtiList = Arrays.asList(mobti.split(""));

        List<Challenge> challenges = challengeRepository
                .findByIsSystemGeneratedTrueAndMobtiTypeIn(mobtiList);

        return challenges.stream()
                .map(challenge -> ChallengeResponse.builder()
                        .id(challenge.getId())
                        .title(challenge.getTitle())
                        .description(challenge.getDescription())
                        .period(challenge.getPeriod())
                        .mobtiType(challenge.getMobtiType())
                        .category(challenge.getCategory())
                        .build())
                .toList();
    }
}
