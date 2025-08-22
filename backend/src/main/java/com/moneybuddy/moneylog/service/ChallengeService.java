package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.dto.request.ChallengeFilterRequest;
import com.moneybuddy.moneylog.dto.response.ChallengeDetailResponse;
import com.moneybuddy.moneylog.dto.response.RecommendedChallengeResponse;
import com.moneybuddy.moneylog.dto.request.UserChallengeRequest;
import com.moneybuddy.moneylog.dto.response.ChallengeCardResponse;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeRepository;
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

    public List<ChallengeCardResponse> getSharedChallenges(Long userId) {
        List<Challenge> challenges = challengeRepository.findByIsSharedTrue();

        return challenges.stream()
                .map(challenge -> ChallengeCardResponse.builder()
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

    public ChallengeCardResponse toChallengeCardResponse(Challenge c) {
        return ChallengeCardResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .category(c.getCategory())
                .type(c.getType())
                .goalType(c.getGoalType())
                .goalPeriod(c.getGoalPeriod())
                .goalValue(c.getGoalValue())
                .isAccountLinked(c.getIsAccountLinked())
                .isMine(false)
                .completed(false)  // 필터링에서는 기본값 false
                .success(false)    // 필터링에서는 기본값 false
                .build();
    }

    public List<ChallengeCardResponse> filterMobtiRecommendedChallenges(Long userId, ChallengeFilterRequest request) {
        String mobti = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"))
                .getMobti();

        List<String> mobtiList = Arrays.asList(mobti.split(""));

        // 먼저 mobti 기준으로 챌린지 가져오기
        List<Challenge> challenges = challengeRepository
                .findByIsSystemGeneratedTrueAndMobtiTypeIn(mobtiList);

        // 추가 조건: type / category / isAccountLinked 필터링
        String type = request.getType();
        String category = request.getCategory();
        Boolean isAccountLinked = request.getIsAccountLinked();

        return challenges.stream()
                .filter(c -> type == null || c.getType().equals(type))
                .filter(c -> {
                    if ("저축".equals(type)) {
                        return "저축".equals(c.getCategory()); // 카테고리 고정
                    } else if ("지출".equals(type)) {
                        boolean categoryMatch = category == null || c.getCategory().equals(category);
                        boolean linkedMatch = isAccountLinked == null || isAccountLinked.equals(c.getIsAccountLinked());
                        return categoryMatch && linkedMatch;
                    }
                    return true;
                })
                .map(this::toChallengeCardResponse)
                .collect(Collectors.toList());
    }

    public List<ChallengeCardResponse> filterSharedChallenges(ChallengeFilterRequest request) {
        List<Challenge> challenges;

        String type = request.getType();
        String category = request.getCategory();
        Boolean isAccountLinked = request.getIsAccountLinked();

        // type은 필수니까 null 체크는 안 함

        if ("저축".equals(type)) {
            // 저축일 경우: category는 "저축" 고정, isAccountLinked는 무시
            challenges = challengeRepository.findByTypeAndCategoryAndIsSharedTrue(type, "저축");

        } else if ("지출".equals(type)) {
            if (category != null && isAccountLinked != null) {
                challenges = challengeRepository.findByTypeAndCategoryAndIsAccountLinkedAndIsSharedTrue(type, category, isAccountLinked);
            } else if (category != null) {
                challenges = challengeRepository.findByTypeAndCategoryAndIsSharedTrue(type, category);
            } else {
                challenges = challengeRepository.findByTypeAndIsSharedTrue(type);
            }

        } else {
            challenges = challengeRepository.findByTypeAndIsSharedTrue(type);
        }

        // Challenge → SharedChallengeResponse로 변환
        return challenges.stream()
                .map(this::toSharedChallengeResponse)
                .collect(Collectors.toList());
    }

    private ChallengeCardResponse toSharedChallengeResponse(Challenge challenge) {
        return ChallengeCardResponse.builder()
                .challengeId(challenge.getId())
                .title(challenge.getTitle())
                .category(challenge.getCategory())
                .type(challenge.getType())
                .goalType(challenge.getGoalType())
                .goalPeriod(challenge.getGoalPeriod())
                .goalValue(challenge.getGoalValue())
                .isAccountLinked(challenge.getIsAccountLinked())
                .isMine(false) // 기본값 false (필요하면 사용자 id 받아서 비교 가능)
                .build();
    }


}
