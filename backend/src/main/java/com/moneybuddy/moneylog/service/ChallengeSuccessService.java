package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.*;
import com.moneybuddy.moneylog.dto.response.ChallengeRewardResponse;
import com.moneybuddy.moneylog.dto.response.ChallengeStatusResponse;
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

    public ChallengeStatusResponse updateTodayStatus(Long userId, Long challengeId, boolean isTodayCompleted) {
        LocalDate today = LocalDate.now();

        // 챌린지 엔티티 먼저 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        // 참여 기록 조회
        UserChallenge userChallenge = userChallengeRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("참여한 챌린지를 찾을 수 없습니다."));

        String message;

        if (isTodayCompleted) {
            // 중복 방지
            if (successRepository.existsByUserIdAndChallengeIdAndSuccessDate(userId, challengeId, today)) {
                throw new IllegalStateException("오늘은 이미 성공했습니다.");
            }

            // 오늘 성공 기록 저장
            successRepository.save(UserChallengeSuccess.builder()
                    .userId(userId)
                    .challenge(challenge)
                    .successDate(today)
                    .build());

            // 유효 기간 계산
            LocalDate start = userChallenge.getJoinedAt().toLocalDate();
            LocalDate end = start.plusDays(parsePeriodToDays(challenge.getGoalPeriod()));

            // 성공 횟수 계산
            long successCount = successRepository.countByUserIdAndChallengeIdAndSuccessDateBetween(
                    userId, challengeId, start, end.minusDays(1)
            );

            // 목표 달성하면 보상
            if (!userChallenge.getCompleted() && successCount >= challenge.getGoalValue()) {
                userChallenge.setCompleted(true);
                userChallenge.setRewarded(true);
                userChallengeRepository.save(userChallenge);

                UserExp userExp = userExpRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("사용자의 경험치 정보를 찾을 수 없습니다."));
                userExp.addExperience(EXP_PER_SUCCESS, EXP_PER_LEVEL);
                userExpRepository.save(userExp);

                message = "축하합니다! 챌린지를 성공하고 경험치 " + EXP_PER_SUCCESS + "점을 획득했습니다!";
            } else {
                message = "하루 성공 기록 완료!";
            }

        } else {
            // 성공 기록 삭제
            successRepository.deleteByUserIdAndChallengeIdAndSuccessDate(userId, challengeId, today);
            message = "하루 성공 기록이 취소되었습니다.";
        }

        int currentDay = successRepository.countByUserIdAndChallengeId(userId, challengeId);

        return new ChallengeStatusResponse(true, message, currentDay);
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