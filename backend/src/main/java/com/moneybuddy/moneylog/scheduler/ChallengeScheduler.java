package com.moneybuddy.moneylog.scheduler;

import com.moneybuddy.moneylog.domain.Challenge;
import com.moneybuddy.moneylog.domain.UserChallenge;
import com.moneybuddy.moneylog.model.NotificationAction;
import com.moneybuddy.moneylog.model.NotificationType;
import com.moneybuddy.moneylog.model.TargetType;
import com.moneybuddy.moneylog.port.Notifier;
import com.moneybuddy.moneylog.repository.UserChallengeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChallengeScheduler {

    private final UserChallengeRepository userChallengeRepository;
    private final Notifier notifier;

    // 매일 새벽 1시에 실행
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void checkExpiredChallenges() {
        LocalDate today = LocalDate.now();

        // 아직 완료되지 않은 챌린지 전부 가져오기
        List<UserChallenge> candidates = userChallengeRepository.findAllIncomplete();

        for (UserChallenge uc : candidates) {
            Challenge c = uc.getChallenge();

            LocalDate start = uc.getJoinedAt().toLocalDate();
            LocalDate end = start.plusDays(parseGoalPeriod(c.getGoalPeriod())); // 문자열 파싱

            // 오늘이 종료일 이후인데 아직 완료 X → 실패 처리
            if (today.isAfter(end)) {
                uc.setCompleted(true);
                uc.setRewarded(false);
                userChallengeRepository.save(uc);

                notifier.send(
                        uc.getUserId(),
                        NotificationType.CHALLENGE_FAIL,
                        TargetType.CHALLENGE,
                        c.getId(),
                        "챌린지 실패",
                        c.getTitle() + " 챌린지를 끝내지 못했어요. 다음에 다시 도전해보세요!",
                        NotificationAction.OPEN_CHALLENGE_DETAIL,
                        Map.of("challengeId", c.getId()),
                        "/challenges/" + c.getId()
                );
            }
        }
    }

    // 챌린지 기간 일(day) 단위로 변환
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
