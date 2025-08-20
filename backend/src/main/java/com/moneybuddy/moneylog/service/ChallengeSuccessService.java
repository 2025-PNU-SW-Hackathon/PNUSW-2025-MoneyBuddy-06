package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.*;
import com.moneybuddy.moneylog.dto.response.ChallengeResultListResponse;
import com.moneybuddy.moneylog.dto.response.ChallengeResultResponse;
import com.moneybuddy.moneylog.dto.response.ChallengeRewardResponse;
import com.moneybuddy.moneylog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeSuccessService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeSuccessRepository successRepository;
    private final UserRepository userRepository;
    private final UserExpRepository userExpRepository; // ✅ 경험치 저장소 추가

    private final int EXP_PER_SUCCESS = 25;
    private final int EXP_PER_LEVEL = 100;

    // ✅ 챌린지 성공 기록 및 보상 처리
    public ChallengeRewardResponse recordSuccess(Long userId, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();

        boolean alreadySuccess = successRepository.existsByUserIdAndChallengeIdAndSuccessDate(userId, challengeId, today);
        if (alreadySuccess) {
            throw new IllegalStateException("오늘은 이미 성공했습니다.");
        }

        successRepository.save(UserChallengeSuccess.builder()
                .userId(userId)
                .challenge(challenge)
                .successDate(today)
                .build());

        UserChallenge userChallenge = userChallengeRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("참여한 챌린지를 찾을 수 없습니다."));

        LocalDate start = userChallenge.getJoinedAt().toLocalDate();
        LocalDate end = start.plusDays(parsePeriodToDays(challenge.getGoalPeriod()));

        long successCount = successRepository.countByUserIdAndChallengeIdAndSuccessDateBetween(
                userId, challengeId, start, end.minusDays(1)
        );

        if (!userChallenge.getCompleted() && successCount >= challenge.getGoalValue()) {
            userChallenge.setCompleted(true);
            userChallengeRepository.save(userChallenge);
        }

        // 경험치 부여
        UserExp userExp = userExpRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자의 경험치 정보를 찾을 수 없습니다."));
        userExp.addExperience(EXP_PER_SUCCESS, EXP_PER_LEVEL);
        userExpRepository.save(userExp);

        return ChallengeRewardResponse.builder()
                .challengeId(challengeId)
                .title(challenge.getTitle())
                .rewarded(true)
                .rewardMessage("축하합니다! 경험치 " + EXP_PER_SUCCESS + "점 획득!")
                .build();
    }

    // 챌린지 결과 조회 API
    public ChallengeResultListResponse getChallengeResults(Long userId) {
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserId(userId);

        List<ChallengeResultResponse> successList = userChallenges.stream()
                .filter(UserChallenge::getCompleted)
                .map(uc -> new ChallengeResultResponse(
                        uc.getChallenge().getId(),
                        uc.getChallenge().getTitle(),
                        true,
                        getLatestSuccessDate(userId, uc.getChallenge().getId())
                ))
                .collect(Collectors.toList());

        List<ChallengeResultResponse> failedList = userChallenges.stream()
                .filter(uc -> !uc.getCompleted())
                .map(uc -> new ChallengeResultResponse(
                        uc.getChallenge().getId(),
                        uc.getChallenge().getTitle(),
                        false,
                        null
                ))
                .collect(Collectors.toList());

        return new ChallengeResultListResponse(successList, failedList);
    }

    private int parsePeriodToDays(String periodStr) {
        if (periodStr.endsWith("일")) {
            return Integer.parseInt(periodStr.replace("일", ""));
        } else if (periodStr.endsWith("주")) {
            return Integer.parseInt(periodStr.replace("주", "")) * 7;
        } else if (periodStr.endsWith("개월")) {
            return Integer.parseInt(periodStr.replace("개월", "")) * 30;
        } else {
            throw new IllegalArgumentException("goalPeriod 형식이 잘못되었습니다: " + periodStr);
        }
    }

    private LocalDate getLatestSuccessDate(Long userId, Long challengeId) {
        return successRepository.findTopByUserIdAndChallengeIdOrderBySuccessDateDesc(userId, challengeId)
                .map(UserChallengeSuccess::getSuccessDate)
                .orElse(null);
    }
}