package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.*;
import com.moneybuddy.moneylog.dto.response.ChallengeRewardResponse;
import com.moneybuddy.moneylog.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ChallengeSuccessService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeSuccessRepository successRepository;
    private final UserExpRepository userExpRepository;

    private static final int EXP_PER_SUCCESS = 25;
    private static final int EXP_PER_LEVEL = 100;

    @Transactional
    public ChallengeRewardResponse recordSuccess(Long userId, Long challengeId) {
        // 챌린지 정보 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        // 오늘 날짜 기준 중복 성공 체크
        LocalDate today = LocalDate.now();
        if (successRepository.existsByUserIdAndChallengeIdAndSuccessDate(userId, challengeId, today)) {
            throw new IllegalStateException("오늘은 이미 성공했습니다.");
        }

        // 성공 기록 저장
        successRepository.save(UserChallengeSuccess.builder()
                .userId(userId)
                .challenge(challenge)
                .successDate(today)
                .build());

        // 유저 챌린지 정보 조회
        UserChallenge userChallenge = userChallengeRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("참여한 챌린지를 찾을 수 없습니다."));

        // 챌린지 시작일 ~ 종료일 계산
        LocalDate start = userChallenge.getJoinedAt().toLocalDate();
        LocalDate end = start.plusDays(parsePeriodToDays(challenge.getGoalPeriod()));

        // 유효 기간 내 성공 횟수 계산
        long successCount = successRepository.countByUserIdAndChallengeIdAndSuccessDateBetween(
                userId, challengeId, start, end.minusDays(1)
        );

        // 완료 조건 판단 + 보상 처리
        String rewardMessage = "하루 성공 기록 완료!";
        if (!userChallenge.getCompleted() && successCount >= challenge.getGoalValue()) {
            userChallenge.setCompleted(true);
            userChallenge.setRewarded(true);
            userChallengeRepository.save(userChallenge);

            // 경험치 지급 및 레벨업 처리
            UserExp userExp = userExpRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자의 경험치 정보를 찾을 수 없습니다."));
            userExp.addExperience(EXP_PER_SUCCESS, EXP_PER_LEVEL);
            userExpRepository.save(userExp);

            rewardMessage = "축하합니다! 챌린지를 성공하고 경험치 " + EXP_PER_SUCCESS + "점을 획득했습니다!";
        }

        // 응답 반환
        return ChallengeRewardResponse.builder()
                .challengeId(challengeId)
                .title(challenge.getTitle())
                .rewarded(userChallenge.getRewarded())
                .rewardMessage(rewardMessage)
                .build();
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
}