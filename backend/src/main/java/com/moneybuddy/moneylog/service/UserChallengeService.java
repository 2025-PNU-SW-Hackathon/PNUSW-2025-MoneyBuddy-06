package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.dto.response.ChallengeProgressResponse;
import com.moneybuddy.moneylog.dto.response.UserChallengeResponse;
import com.moneybuddy.moneylog.repository.ChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeRepository;
import com.moneybuddy.moneylog.repository.UserChallengeSuccessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserChallengeService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeSuccessRepository userChallengeSuccessRepository;

    public UserChallengeResponse joinChallenge(Long userId, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지를 찾을 수 없습니다."));

        boolean alreadyJoined = userChallengeRepository.existsByUserIdAndChallengeId(userId, challengeId);
        if (alreadyJoined) {
            throw new IllegalStateException("이미 이 챌린지에 참여하고 있습니다.");
        }

        UserChallenge userChallenge = new UserChallenge();
        userChallenge.setUserId(userId);
        userChallenge.setChallenge(challenge);
        userChallenge.setJoinedAt(LocalDateTime.now());

        userChallengeRepository.save(userChallenge);

        return toResponse(userChallenge);
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
                .isShared(c.getIsShared())
                .joinedAt(userChallenge.getJoinedAt())
                .completed(userChallenge.getCompleted())
                .rewarded(userChallenge.getRewarded())
                .build();
    }

    public List<ChallengeProgressResponse> getOngoingChallenges(Long userId) {
        return userChallengeRepository.findByUserIdAndCompletedFalse(userId).stream()
                .map(this::toProgressResponse)
                .collect(Collectors.toList());
    }

    public List<ChallengeProgressResponse> getCompletedChallenges(Long userId) {
        return userChallengeRepository.findByUserIdAndCompletedTrue(userId).stream()
                .map(userChallenge -> {
                    Challenge c = userChallenge.getChallenge();
                    LocalDate start = userChallenge.getJoinedAt().toLocalDate();
                    LocalDate end = start.plusDays(parsePeriodToDays(c.getGoalPeriod()));

                    long successCount = userChallengeSuccessRepository
                            .countByUserIdAndChallengeIdAndSuccessDateBetween(
                                    userId, c.getId(), start, end.minusDays(1)
                            );

                    boolean isSuccess = successCount >= c.getGoalValue();

                    return ChallengeProgressResponse.builder()
                            .challengeId(c.getId())
                            .title(c.getTitle())
                            .goalPeriod(c.getGoalPeriod())
                            .goalValue(c.getGoalValue())
                            .completed(true)
                            .success(isSuccess)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public ChallengeProgressResponse toProgressResponse(UserChallenge userChallenge) {
        Challenge c = userChallenge.getChallenge();

        return ChallengeProgressResponse.builder()
                .challengeId(c.getId())
                .title(c.getTitle())
                .goalPeriod(c.getGoalPeriod())
                .goalValue(c.getGoalValue())
                .completed(userChallenge.getCompleted())
                .success(false) // 진행 중 챌린지 → 아직 성공 여부 판단 불필요
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
