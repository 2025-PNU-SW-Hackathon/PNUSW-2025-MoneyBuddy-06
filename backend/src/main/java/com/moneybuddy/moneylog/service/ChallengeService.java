package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.dto.response.ChallengeDetailResponse;
import com.moneybuddy.moneylog.dto.response.RecommendedChallengeResponse;
import com.moneybuddy.moneylog.dto.request.UserChallengeRequest;
import com.moneybuddy.moneylog.dto.response.SharedChallengeResponse;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;

    public List<RecommendedChallengeResponse> getRecommendedChallenges(Long userId) {
        String mobti = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."))
                .getMobti();

        List<String> mobtiList = Arrays.asList(mobti.split(""));

        List<Challenge> challenges = challengeRepository
                .findByIsSystemGeneratedTrueAndMobtiTypeIn(mobtiList);

        return challenges.stream()
                .map(challenge -> RecommendedChallengeResponse.builder()
                        .id(challenge.getId())
                        .title(challenge.getTitle())
                        .description(challenge.getDescription())
                        .type(challenge.getType())
                        .mobtiType(challenge.getMobtiType())
                        .goalPeriod(challenge.getGoalPeriod())
                        .goalType(challenge.getGoalType())
                        .goalValue(challenge.getGoalValue())
                        .isSystemGenerated(true)
                        .isAccountLinked((challenge.getIsAccountLinked()))
                        .build())
                .toList();
    }

    public void createUserChallenge(Long userId, UserChallengeRequest request) {
        Challenge challenge = Challenge.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .category(request.getCategory())
                .goalPeriod(request.getGoalPeriod())
                .goalType(request.getGoalType())
                .goalValue(request.getGoalValue())
                .isSystemGenerated(false)       // 사용자 생성 챌린지
                .isShared(request.getIsShared())// 개인용 or 공유용 여부
                .createdBy(userId)
                .isAccountLinked((request.getIsAccountLinked()))
                .build();

        challengeRepository.save(challenge);
    }

    public List<SharedChallengeResponse> getSharedChallenges(Long userId) {
        List<Challenge> challenges = challengeRepository.findByIsSharedTrue();

        return challenges.stream()
                .map(challenge -> SharedChallengeResponse.builder()
                        .challengeId(challenge.getId())
                        .title(challenge.getTitle())
                        .goalPeriod(challenge.getGoalPeriod())
                        .goalValue(challenge.getGoalValue())
                        .isMine(challenge.getCreatedBy().equals(userId))
                        .build())
                .toList();
    }

    public ChallengeDetailResponse getChallengeDetail(Long challengeId, Long userId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        boolean isJoined = userChallengeRepository.existsByUserIdAndChallengeId(userId, challengeId);
        int participantCount = userChallengeRepository.countByChallengeId(challengeId);

        return ChallengeDetailResponse.builder()
                .challengeId(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .goalPeriod(challenge.getGoalPeriod())
                .goalType(challenge.getGoalType())
                .goalValue(challenge.getGoalValue())
                .currentParticipants(participantCount)
                .isJoined(isJoined)
                .build();
    }
}
