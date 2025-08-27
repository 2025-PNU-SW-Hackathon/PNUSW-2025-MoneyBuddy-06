package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.domain.UserChallenge;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;

    /**
     *  사용자의 MoBTI 값을 기반으로 추천 챌린지 목록 조회
     */
    public List<RecommendedChallengeResponse> getRecommendedChallenges(Long userId) {
        String mobti = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."))
                .getMobti();

        //  MoBTI를 한 글자씩 나눔
        List<String> mobtiList = Arrays.asList(mobti.split(""));

        //  시스템에서 생성된 챌린지 중 해당 mobti가 포함된 것만 조회
        List<Challenge> challenges = challengeRepository
                .findByIsSystemGeneratedTrueAndMobtiTypeIn(mobtiList);

        //  Challenge → RecommendedChallengeResponse 변환
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

    /**
     * 사용자가 직접 챌린지를 생성할 때 호출됨
     */
    public void createUserChallenge(Long userId, UserChallengeRequest request) {
        Challenge challenge = Challenge.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .category(request.getCategory())
                .goalPeriod(request.getGoalPeriod())
                .goalType(request.getGoalType())
                .goalValue(request.getGoalValue())
                .isSystemGenerated(false)
                .isShared(request.getIsShared())
                .createdBy(userId)
                .isAccountLinked(false)
                .build();

        // 먼저 Challenge 저장
        Challenge savedChallenge = challengeRepository.save(challenge);

        // 생성한 챌린지에 자동 참여 등록 (UserChallenge)
        UserChallenge userChallenge = UserChallenge.builder()
                .userId(userId)
                .challenge(savedChallenge)
                .joinedAt(LocalDateTime.now())
                .completed(false)
                .rewarded(false)
                .success(false)
                .build();
        userChallengeRepository.save(userChallenge);
    }

    /**
     * 공유 챌린지 전체 목록 조회
     */
    public List<ChallengeCardResponse> getSharedChallenges(Long userId) {
        List<Challenge> challenges = challengeRepository.findByIsSharedTrue();

        return challenges.stream()
                .map(challenge -> ChallengeCardResponse.builder()
                        .challengeId(challenge.getId())
                        .title(challenge.getTitle())
                        .goalPeriod(challenge.getGoalPeriod())
                        .goalValue(challenge.getGoalValue())
                        .isMine(challenge.getCreatedBy() != null && challenge.getCreatedBy().equals(userId))
                        .build())
                .toList();
    }

    /**
     * 챌린지 상세 정보 조회
     */
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

    /**
     *  MoBTI 기반 추천 챌린지 필터링
     */
    public List<ChallengeCardResponse> filterMobtiRecommendedChallenges(Long userId, ChallengeFilterRequest request) {
        String mobti = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"))
                .getMobti();

        List<String> mobtiList = Arrays.asList(mobti.split(""));

        // 먼저 mobti 기준으로 챌린지 가져오기
        List<Challenge> challenges = challengeRepository
                .findByIsSystemGeneratedTrueAndMobtiTypeIn(mobtiList);

        // type / category / isAccountLinked 필터링
        String type = request.getType();
        String category = request.getCategory();
        Boolean isAccountLinked = request.getIsAccountLinked();

        return challenges.stream()
                .filter(c -> type == null || c.getType().equals(type))
                .filter(c -> {
                    if ("저축".equals(type)) {
                        return "저축".equals(c.getCategory());
                    } else if ("지출".equals(type)) {
                        boolean categoryMatch = category == null || c.getCategory().equals(category);
                        boolean linkedMatch = isAccountLinked == null || isAccountLinked.equals(c.getIsAccountLinked());
                        return categoryMatch && linkedMatch;
                    }
                    return true;
                })
                .map(this::toRecommendedChallengeCardResponse)
                .collect(Collectors.toList());
    }

    /**
     *  추천 챌린지 필터링 결과에 사용
     */
    public ChallengeCardResponse toRecommendedChallengeCardResponse(Challenge c) {
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
                .completed(false)
                .success(false)
                .build();
    }

    /**
     * 공유 챌린지 필터링
     */
    public List<ChallengeCardResponse> filterSharedChallenges(ChallengeFilterRequest request) {
        List<Challenge> challenges;

        String type = request.getType();
        String category = request.getCategory();

        if ("저축".equals(type)) {
            challenges = challengeRepository.findByTypeAndCategoryAndIsSharedTrue(type, "저축");
        } else if ("지출".equals(type)) {
            if (category != null) {
                challenges = challengeRepository.findByTypeAndCategoryAndIsSharedTrue(type, category);
            } else {
                challenges = challengeRepository.findByTypeAndIsSharedTrue(type);
            }
        } else {
            challenges = challengeRepository.findByTypeAndIsSharedTrue(type);
        }

        return challenges.stream()
                .map(this::toSharedChallengeResponse)
                .collect(Collectors.toList());
    }

    /**
     *  공유 챌린지 필터링 결과에 사용
     */
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
                .isMine(false)
                .build();
    }


}
