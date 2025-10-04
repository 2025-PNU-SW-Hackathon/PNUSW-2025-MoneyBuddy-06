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
import com.moneybuddy.moneylog.repository.UserChallengeSuccessRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserChallengeSuccessRepository userChallengeSuccessRepository;

    // 사용자의 MoBTI 값을 기반으로 추천 챌린지 목록 조회
    public List<RecommendedChallengeResponse> getRecommendedChallenges(Long userId) {
        String mobti = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."))
                .getMobti();

        // MoBTI를 한 글자씩 나눔
        List<String> mobtiList = Arrays.asList(mobti.split(""));

        // 시스템 생성 + mobti 포함 챌린지 조회
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
                        .isAccountLinked(challenge.isAccountLinked())
                        .build())
                .toList();
    }

    // 사용자가 직접 챌린지를 생성할 때 호출됨
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
                .isShared(request.getIsShared() != null && request.getIsShared())
                .createdBy(userId)
                .isAccountLinked(false)
                .build();

        // Challenge 저장
        Challenge savedChallenge = challengeRepository.save(challenge);

        // UserChallenge 자동 생성
        UserChallenge userChallenge = UserChallenge.builder()
                .userId(userId)
                .challenge(savedChallenge)
                .joinedAt(LocalDateTime.now())
                .completed(false)
                .rewarded(false)
                .build();
        userChallengeRepository.save(userChallenge);
    }

    // 공유 챌린지 전체 목록 조회
    public List<ChallengeCardResponse> getSharedChallenges(Long userId) {
        List<Challenge> challenges = challengeRepository.findByIsSharedTrue();

        return challenges.stream()
                .map(challenge -> toSharedChallengeResponse(challenge, userId))
                .toList();
    }

    // 챌린지 상세 정보 조회
    public ChallengeDetailResponse getChallengeDetail(Long challengeId, Long userId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        Optional<UserChallenge> userChallengeOpt =
                userChallengeRepository.findByUserIdAndChallengeId(userId, challengeId);

        int participantCount = userChallengeRepository.countByChallengeId(challengeId);

        boolean isJoined = userChallengeOpt.isPresent();
        boolean completed = false;
        boolean rewarded = false;
        LocalDateTime joinedAt = null;

        long successCount = 0;

        if (userChallengeOpt.isPresent()) {
            UserChallenge uc = userChallengeOpt.get();
            completed = uc.isCompleted();
            rewarded = uc.isRewarded();
            joinedAt = uc.getJoinedAt();

            LocalDate start = uc.getJoinedAt().toLocalDate();
            LocalDate end = start.plusDays(parseGoalPeriod(challenge.getGoalPeriod()));

            successCount = userChallengeSuccessRepository
                    .countByUserIdAndChallengeIdAndSuccessDateBetween(
                            userId, challengeId, start, end.minusDays(1)
                    );
        }

        boolean success = successCount >= challenge.getGoalValue(); // 목표 달성 여부

        return ChallengeDetailResponse.builder()
                .challengeId(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .type(challenge.getType())
                .category(challenge.getCategory())
                .goalPeriod(challenge.getGoalPeriod())
                .goalType(challenge.getGoalType())
                .goalValue(challenge.getGoalValue())
                .mobtiType(challenge.getMobtiType())
                .isSystemGenerated(challenge.isSystemGenerated())
                .isAccountLinked(challenge.isAccountLinked())
                .createdBy(challenge.getCreatedBy())
                .currentParticipants(participantCount)

                .isJoined(isJoined)
                .joinedAt(joinedAt)
                .completed(completed)
                .success(success)
                .rewarded(rewarded)
                .mine(challenge.getCreatedBy() != null && challenge.getCreatedBy().equals(userId))

                .build();
    }

    // MoBTI 기반 추천 챌린지 필터링
    public List<ChallengeCardResponse> filterMobtiRecommendedChallenges(Long userId, ChallengeFilterRequest request) {
        String mobti = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"))
                .getMobti();

        List<String> mobtiList = Arrays.asList(mobti.split(""));
        List<Challenge> challenges = challengeRepository
                .findByIsSystemGeneratedTrueAndMobtiTypeIn(mobtiList);

        String category = normalizeNullable(request.getCategory()); // 기대값: "기타" 또는 "저축"

        if (category == null) {
            return Collections.emptyList();
        }

        return challenges.stream()
                .filter(c -> Objects.equals(c.getCategory(), category))
                .map(this::toRecommendedChallengeCardResponse)
                .collect(Collectors.toList());
    }

    // 추천 챌린지 -> 카드 응답 변환
    public ChallengeCardResponse toRecommendedChallengeCardResponse(Challenge c) {
        return ChallengeCardResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .category(c.getCategory())
                .type(c.getType())
                .goalType(c.getGoalType())
                .goalPeriod(c.getGoalPeriod())
                .goalValue(c.getGoalValue())
                .isAccountLinked(c.isAccountLinked())
                .mine(false)
                .completed(false)
                .success(false)
                .build();
    }

    private String normalizeNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty() || "전체".equals(t) || "ALL".equalsIgnoreCase(t)) return null;
        return t;
    }

    // 공유 챌린지 필터링
    public List<ChallengeCardResponse> filterSharedChallenges(Long userId, ChallengeFilterRequest request) {
        String category = normalizeNullable(request.getCategory());

        if (category == null) {
            return Collections.emptyList();
        }

        List<Challenge> challenges = challengeRepository.findByCategoryAndIsSharedTrue(category);

        return challenges.stream()
                .map(c -> toSharedChallengeResponse(c, userId))
                .collect(Collectors.toList());
    }

    // 공유 챌린지 -> 카드 응답 변환
    private ChallengeCardResponse toSharedChallengeResponse(Challenge challenge, Long userId) {
        int participantCount = userChallengeRepository.countByChallengeId(challenge.getId());
        boolean isJoined = userChallengeRepository.existsByUserIdAndChallengeId(userId, challenge.getId());

        return ChallengeCardResponse.builder()
                .challengeId(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .type(challenge.getType())
                .category(challenge.getCategory())
                .goalPeriod(challenge.getGoalPeriod())
                .goalType(challenge.getGoalType())
                .goalValue(challenge.getGoalValue())
                .isSystemGenerated(challenge.isSystemGenerated())
                .isAccountLinked(challenge.isAccountLinked())
                .createdBy(challenge.getCreatedBy())
                .mine(challenge.getCreatedBy() != null && challenge.getCreatedBy().equals(userId))

                .isJoined(isJoined)
                .joinedAt(null)
                .currentParticipants(participantCount)

                .completed(false)
                .success(false)
                .rewarded(false)

                .mobtiType(challenge.getMobtiType())
                .build();
    }

    // 챌린지 기간을 일(day) 단위로 환산
    private int parseGoalPeriod(String periodStr) {
        if (periodStr == null || periodStr.isEmpty()) {
            throw new IllegalArgumentException("goalPeriod 값이 없습니다.");
        }

        if (periodStr.endsWith("일")) {
            return Integer.parseInt(periodStr.replace("일", "").trim());
        } else if (periodStr.endsWith("주")) {
            return Integer.parseInt(periodStr.replace("주", "").trim()) * 7;
        } else if (periodStr.endsWith("개월") || periodStr.endsWith("달")) {
            return Integer.parseInt(periodStr.replaceAll("개월|달", "").trim()) * 30;
        } else {
            throw new IllegalArgumentException("goalPeriod 형식이 잘못되었습니다: " + periodStr);
        }
    }
}
