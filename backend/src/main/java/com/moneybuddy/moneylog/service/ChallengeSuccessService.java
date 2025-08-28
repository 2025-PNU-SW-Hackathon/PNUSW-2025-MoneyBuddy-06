package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.*;
import com.moneybuddy.moneylog.dto.response.ChallengeStatusResponse;
import com.moneybuddy.moneylog.model.NotificationAction;
import com.moneybuddy.moneylog.model.NotificationType;
import com.moneybuddy.moneylog.model.TargetType;
import com.moneybuddy.moneylog.port.Notifier;
import com.moneybuddy.moneylog.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChallengeSuccessService {

    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeSuccessRepository successRepository;
    private final UserExpRepository userExpRepository;
    private final UserRepository userRepository;
    private final Notifier notifier;

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

            // 경험치 지급 (없으면 생성)
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            UserExp userExp = userExpRepository.findById(userId)
                    .orElseGet(() -> {
                        UserExp newExp = UserExp.builder()
                                .user(user)
                                .experience(0)
                                .level(1)
                                .build();
                        return userExpRepository.save(newExp);
                    });

            boolean leveledUp = userExp.addExperience(EXP_PER_SUCCESS, EXP_PER_LEVEL);
            userExpRepository.save(userExp);

            // 챌린지 기간 내 성공 횟수 계산
            LocalDate start = userChallenge.getJoinedAt().toLocalDate();
            LocalDate end = start.plusDays(parseGoalPeriod(challenge.getGoalPeriod()));

            long successCount = successRepository.countByUserIdAndChallengeIdAndSuccessDateBetween(
                    userId, challengeId, start, end.minusDays(1)
            );

            // 목표 달성 시 최종 성공 처리
            if (!userChallenge.isCompleted() && successCount >= challenge.getGoalValue()) {
                userChallenge.setCompleted(true);
                userChallenge.setSuccess(true);   // 최종 성공 처리
                userChallenge.setRewarded(true);
                userChallengeRepository.save(userChallenge);

                notifier.send(
                        userId,
                        NotificationType.CHALLENGE_SUCCESS,
                        TargetType.CHALLENGE,
                        challengeId,
                        "챌린지 성공!",
                        challenge.getTitle() + " 챌린지를 달성했어요 👏",
                        NotificationAction.OPEN_CHALLENGE_DETAIL,
                        Map.of("challengeId", challengeId),
                        "/challenges/" + challengeId
                );

                if (leveledUp) {
                    notifier.send(
                            userId,
                            NotificationType.LEVEL_UP,
                            TargetType.PROFILE,
                            null,
                            "레벨 업! 🎉",
                            "새 레벨에 도달했습니다. 레벨을 확인해보세요.",
                            NotificationAction.OPEN_PROFILE_LEVEL,
                            Map.of("newLevel", userExp.getLevel()),
                            "/profile/level"
                    );
                }

                message = "축하합니다! 챌린지를 성공하고 경험치 " + EXP_PER_SUCCESS + "점을 획득했습니다!";
            } else {
                message = "하루 성공 기록 완료!";
            }

        } else {
            // 하루 성공 기록 취소
            successRepository.deleteByUserIdAndChallengeIdAndSuccessDate(userId, challengeId, today);

            // 전체 성공 횟수 재계산
            LocalDate start = userChallenge.getJoinedAt().toLocalDate();
            LocalDate end = start.plusDays(parseGoalPeriod(challenge.getGoalPeriod()));
            long successCount = successRepository.countByUserIdAndChallengeIdAndSuccessDateBetween(
                    userId, challengeId, start, end.minusDays(1)
            );

            // 목표치 미달이면 최종 성공 상태 되돌리기
            if (successCount < challenge.getGoalValue()) {
                userChallenge.setCompleted(false);
                userChallenge.setSuccess(false);
                userChallenge.setRewarded(false);
                userChallengeRepository.save(userChallenge);
            }

            message = "하루 성공 기록이 취소되었습니다.";
        }

        // 현재까지 총 성공 일수
        int currentDay = successRepository.countByUserIdAndChallengeId(userId, challengeId);

        // 최종 성공 여부(DB 기준)
        boolean finalSuccess = userChallenge.isSuccess();

        // 오늘 성공 여부 (요청값 그대로)
        boolean todaySuccess = isTodayCompleted;

        return new ChallengeStatusResponse(message, currentDay, todaySuccess, finalSuccess);
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