package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Quiz;
import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.domain.UserDailyQuiz;
import com.moneybuddy.moneylog.dto.request.QuizAnswerRequest;
import com.moneybuddy.moneylog.dto.response.QuizResponse;
import com.moneybuddy.moneylog.dto.response.QuizResultResponse;
import com.moneybuddy.moneylog.repository.QuizRepository;
import com.moneybuddy.moneylog.repository.UserDailyQuizRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserDailyQuizRepository userDailyQuizRepository;
    private final UserRepository userRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * 오늘 보여줄 문제
     * 1) 안 푼 배정이 있으면 → 그대로 반환(내일까지 유지)
     * 2) 오늘 이미 풀었으면 → 새 배정 금지
     * 3) 아니면 → 랜덤 배정 생성 후 반환
     */
    @Transactional
    public QuizResponse getUserDailyQuiz(User user) {
        Long userId = user.getId();

        // 아직 안 푼 배정 있으면 그거 반환
        Optional<UserDailyQuiz> activeOpt = userDailyQuizRepository.findFirstByUserIdAndCompletedFalse(userId);
        if (activeOpt.isPresent()) {
            return QuizResponse.from(activeOpt.get().getQuiz());
        }

        // 오늘 이미 풀었으면 새 배정 금지
        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        boolean solvedToday = userDailyQuizRepository
                .existsByUserIdAndCompletedTrueAndAssignedAtBetween(userId, start, end);
        if (solvedToday) {
            throw new IllegalStateException("오늘은 이미 퀴즈를 완료했습니다.");
        }

        // 새 배정
        List<Quiz> all = quizRepository.findAll();
        if (all.isEmpty()) {
            throw new IllegalStateException("등록된 퀴즈가 없습니다.");
        }
        Quiz picked = all.get(new java.util.Random().nextInt(all.size()));

        UserDailyQuiz udq = UserDailyQuiz.builder()
                .user(user) // ← 이미 받은 user 그대로 사용 (FK 채워짐)
                .quiz(picked)
                .assignedAt(LocalDateTime.now())
                .completed(false)
                .build();
        userDailyQuizRepository.save(udq);


        return QuizResponse.from(picked);
    }

    /**
     * 답 제출
     * - 현재 배정된 문제에만 제출 가능
     * - 맞으면 점수 +10
     * - completed=true 처리로 오늘 종료
     */
    @Transactional
    public QuizResultResponse submitAnswer(User user, QuizAnswerRequest request) {
        // 기본 검증
        if (user == null || request == null || request.getQuizId() == null || request.getAnswer() == null) {
            throw new IllegalArgumentException("요청 값이 올바르지 않습니다.");
        }

        // 아직 안 푼 배정 가져오기 (없으면 에러)
        UserDailyQuiz active = userDailyQuizRepository
                .findFirstByUserIdAndCompletedFalse(user.getId())
                .orElseThrow(() -> new IllegalStateException("활성화된 퀴즈가 없습니다. 먼저 문제를 배정받으세요."));

        Quiz quiz = active.getQuiz();

        // 배정된 문제와 같은지 확인
        if (!quiz.getId().equals(request.getQuizId())) {
            throw new IllegalStateException("현재 배정된 퀴즈와 일치하지 않습니다.");
        }

        // 정답 체크 (Boolean -> boolean 안전 변환)
        boolean userAnswer = Boolean.TRUE.equals(request.getAnswer());
        boolean isCorrect = quiz.getCorrectAnswer().equals(userAnswer);

        // 풀이 결과 저장 + 완료 처리 (JPA dirty checking으로 자동 반영)
        active.setSelectedAnswer(userAnswer);
        active.setCorrect(isCorrect);
        active.setCompleted(true);

        // 점수 업데이트
        boolean scoreUpdated = false;
        Integer newScore = user.getScore();
        if (isCorrect) {
            user.increaseScore(10);
            userRepository.save(user);
            scoreUpdated = true;
            newScore = user.getScore();
        }

        return QuizResultResponse.builder()
                .isCorrect(isCorrect)
                .explanation(quiz.getExplanation())
                .scoreUpdated(scoreUpdated)
                .newScore(newScore)
                .build();
    }

    // 완료한 퀴즈 ID 리스트
    @Transactional(readOnly = true)
    public List<Long> getCompletedQuizIds(Long userId) {
        return userDailyQuizRepository.findCompletedQuizIdsByUserId(userId);
    }

    // 완료한 퀴즈 간단 정보 (id, question)
    @Transactional(readOnly = true)
    public List<QuizResponse> getCompletedQuizzes(Long userId) {
        List<Long> ids = userDailyQuizRepository.findCompletedQuizIdsByUserId(userId);
        if (ids.isEmpty()) return List.of();

        // 필요한 만큼만 가져오기
        List<Quiz> quizzes = quizRepository.findAllById(ids);
        return quizzes.stream()
                .map(QuizResponse::from)
                .toList();
    }
}
