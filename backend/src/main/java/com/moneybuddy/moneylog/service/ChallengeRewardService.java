package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeRewardService {

    private final UserChallengeRepository userChallengeRepository;

    public void rewardChallenge(Long userId, Long challengeId) {
        UserChallenge userChallenge = userChallengeRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("챌린지를 찾을 수 없습니다."));

        if (!userChallenge.getCompleted()) {
            throw new IllegalStateException("아직 완료되지 않은 챌린지입니다.");
        }

        if (userChallenge.getRewarded()) {
            throw new IllegalStateException("이미 보상을 받은 챌린지입니다.");
        }

        // 보상 처리
        userChallenge.setRewarded(true);
        userChallengeRepository.save(userChallenge);

    }
}
