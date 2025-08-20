package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.domain.UserChallengeSuccess;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeSuccessRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ChallengeSuccessService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeSuccessRepository successRepository;
    private final UserRepository userRepository; // ✅ 추가

    private final int EXP_PER_SUCCESS = 25;
    private final int EXP_PER_LEVEL = 100;

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
                userId, challengeId, start, end.minusDays(1)
        );

        if (!userChallenge.getCompleted() && successCount >= challenge.getGoalValue()) {
            userChallenge.setCompleted(true);
            userChallengeRepository.save(userChallenge);
        }

        // ✅ 사용자 경험치 부여
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        addExperience(user);
        userRepository.save(user); // 저장!
    }

    private void addExperience(User user) {
        int newExp = user.getExperience() + EXP_PER_SUCCESS;
        if (newExp >= EXP_PER_LEVEL) {
            user.setLevel(user.getLevel() + 1);
            user.setExperience(newExp - EXP_PER_LEVEL); // 남은 경험치 유지
        } else {
            user.setExperience(newExp);
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