package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.domain.UserChallengeSuccess;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeSuccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ChallengeSuccessService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeSuccessRepository successRepository;

    public void recordSuccess(Long userId, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        LocalDate today = LocalDate.now();

        // 중복 성공 방지
        boolean alreadySuccess = successRepository.existsByUserIdAndChallengeIdAndSuccessDate(userId, challengeId, today);
        if (alreadySuccess) {
            throw new IllegalStateException("오늘은 이미 성공했습니다.");
        }

        // 성공 기록 저장
        successRepository.save(UserChallengeSuccess.builder()
                .userId(userId)
                .challenge(challenge)
                .successDate(today)
                .build());

        // 참여 정보 가져오기
        UserChallenge userChallenge = userChallengeRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("참여한 챌린지를 찾을 수 없습니다."));

        // 참여일 기준 goalPeriod 계산
        LocalDate start = userChallenge.getJoinedAt().toLocalDate();
        LocalDate end = start.plusDays(parsePeriodToDays(challenge.getGoalPeriod()));

        // 해당 기간 안의 성공 횟수만 집계
        long successCount = successRepository.countByUserIdAndChallengeIdAndSuccessDateBetween(
                userId, challengeId, start, end.minusDays(1)  // end는 exclusive로 처리
        );

        if (!userChallenge.getCompleted()
                && successCount >= challenge.getGoalValue()) {
            userChallenge.setCompleted(true);
            userChallengeRepository.save(userChallenge);
        }
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
