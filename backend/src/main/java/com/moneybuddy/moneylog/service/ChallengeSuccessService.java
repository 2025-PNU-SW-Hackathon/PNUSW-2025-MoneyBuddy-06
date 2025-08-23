package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.*;
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

    // 경험치 상수 정의
    private static final int EXP_PER_SUCCESS = 25;
    private static final int EXP_PER_LEVEL = 100;

    /**
     *  하루 챌린지 성공/취소 상태 업데이트
     */
    @Transactional
    public ChallengeStatusResponse updateTodayStatus(Long userId, Long challengeId, boolean isTodayCompleted) {
        LocalDate today = LocalDate.now();

        // 챌린지 정보 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        // 해당 사용자의 참여 기록 조회
        UserChallenge userChallenge = userChallengeRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("참여한 챌린지를 찾을 수 없습니다."));

        String message;

        if (isTodayCompleted) {
            // 중복 기록 방지
            if (successRepository.existsByUserIdAndChallengeIdAndSuccessDate(userId, challengeId, today)) {
                throw new IllegalStateException("오늘은 이미 성공했습니다.");
            }

            // 오늘 성공 기록 저장
            successRepository.save(UserChallengeSuccess.builder()
                    .userId(userId)
                    .challenge(challenge)
                    .successDate(today)
                    .build());

            // 챌린지 시작일 ~ 종료일 계산
            LocalDate start = userChallenge.getJoinedAt().toLocalDate();
            LocalDate end = start.plusDays(parseGoalPeriod(challenge.getGoalPeriod()));

            // 유효 기간 내 성공 횟수 계산
            long successCount = successRepository.countByUserIdAndChallengeIdAndSuccessDateBetween(
                    userId, challengeId, start, end.minusDays(1)
            );

            // 목표 달성 시 챌린지 완료 및 보상 지급
            if (!userChallenge.getCompleted() && successCount >= challenge.getGoalValue()) {
                userChallenge.setCompleted(true);
                userChallenge.setRewarded(true);
                userChallengeRepository.save(userChallenge);

                // 경험치 지금
                UserExp userExp = userExpRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("사용자의 경험치 정보를 찾을 수 없습니다."));
                userExp.addExperience(EXP_PER_SUCCESS, EXP_PER_LEVEL);
                userExpRepository.save(userExp);

                message = "축하합니다! 챌린지를 성공하고 경험치 " + EXP_PER_SUCCESS + "점을 획득했습니다!";
            } else {
                message = "하루 성공 기록 완료!";
            }

        } else {
            // 성공 기록 취소
            successRepository.deleteByUserIdAndChallengeIdAndSuccessDate(userId, challengeId, today);
            message = "하루 성공 기록이 취소되었습니다.";
        }

        // 현재까지의 총 성공 일수 반환
        int currentDay = successRepository.countByUserIdAndChallengeId(userId, challengeId);

        return new ChallengeStatusResponse(true, message, currentDay);
    }

    /**
     * 챌린지 기간을 일(day) 단위로 환산
     */
    private int parseGoalPeriod(String periodStr) {
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