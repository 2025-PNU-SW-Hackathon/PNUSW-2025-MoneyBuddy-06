package com.moneybuddy.moneylog.quiz;

import com.moneybuddy.moneylog.domain.Quiz;
import com.moneybuddy.moneylog.model.NotificationAction;
import com.moneybuddy.moneylog.model.NotificationType;
import com.moneybuddy.moneylog.model.TargetType;
import com.moneybuddy.moneylog.port.Notifier;
import com.moneybuddy.moneylog.repository.QuizRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QuizScheduler {

    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final Notifier notifier;

    // 매일 오전 9시 실행
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void pushDailyQuiz() {
        // 오늘 퀴즈가 없으면 스킵
        Quiz quiz = quizRepository.findByQuizDate(LocalDate.now()).orElse(null);
        if (quiz == null) return;

        List<Long> activeUsers = userRepository.findAllIds();

        // 알림/푸시 발송
        for (Long uid : activeUsers) {
            notifier.send(
                    uid,
                    NotificationType.QUIZ_TODAY,
                    TargetType.QUIZ,
                    quiz.getId(),
                    "오늘의 퀴즈가 도착했어요",
                    "딱 한 문제만 풀고 금융 점수를 올려 보세요!",
                    NotificationAction.OPEN_QUIZ_TODAY,
                    Map.of(),
                    null
            );
        }
    }
}
