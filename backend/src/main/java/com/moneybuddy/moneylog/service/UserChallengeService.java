package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.dto.UserChallengeResponse;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserChallengeService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;

    public void joinChallenge(Long userId, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));

        // 중복 참여 방지
        boolean alreadyJoined = userChallengeRepository.existsByUserIdAndChallengeId(userId, challengeId);
        if (alreadyJoined) {
            throw new IllegalStateException("이미 이 챌린지에 참여하고 있습니다.");
        }

        UserChallenge userChallenge = new UserChallenge();
        userChallenge.setUserId(userId);
        userChallenge.setChallenge(challenge);
        userChallenge.setJoinedAt(LocalDateTime.now());

        userChallengeRepository.save(userChallenge);
    }

    public UserChallengeResponse toResponse(UserChallenge userChallenge) {
        Challenge c = userChallenge.getChallenge();

        return UserChallengeResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .type(c.getType())
                .goalPeriod(c.getGoalPeriod())
                .goalType(c.getGoalType())
                .goalValue(c.getGoalValue())
                .isAccountLinked(c.getIsAccountLinked())
                .joinedAt(userChallenge.getJoinedAt())
                .build();
    }
}
