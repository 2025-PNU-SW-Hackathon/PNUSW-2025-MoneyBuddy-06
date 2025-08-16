package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.dto.ChallengeResponse;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public List<ChallengeResponse> getRecommendedChallenges(String mobti) {
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
