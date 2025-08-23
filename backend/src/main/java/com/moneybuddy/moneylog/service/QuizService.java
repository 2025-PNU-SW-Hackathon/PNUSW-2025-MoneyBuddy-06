package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.Quiz;
import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.domain.UserQuiz;
import com.moneybuddy.moneylog.dto.request.QuizAnswerRequest;
import com.moneybuddy.moneylog.dto.response.QuizResponse;
import com.moneybuddy.moneylog.dto.response.QuizResultResponse;
import com.moneybuddy.moneylog.repository.QuizRepository;
import com.moneybuddy.moneylog.repository.UserQuizRepository;
import com.moneybuddy.moneylog.repository.UserRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserQuizRepository userQuizRepository;
    private final UserRepository userRepository;


    public QuizResponse getTodayQuiz(User user) {
        LocalDate today = LocalDate.now();

        Quiz quiz = quizRepository.findByQuizDate(today)
                .orElseThrow(() -> new IllegalArgumentException("오늘의 퀴즈가 존재하지 않습니다."));

        boolean alreadyAnswered = userQuizRepository.findByUserAndQuiz(user, quiz).isPresent();

        return QuizResponse.builder()
                .quizId(quiz.getId())
                .question(quiz.getQuestion())
                .alreadyAnswered(alreadyAnswered)
                .build();
    }


    public QuizResultResponse submitAnswer(User user, QuizAnswerRequest request) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("해당 퀴즈가 존재하지 않습니다."));


        if (userQuizRepository.findByUserAndQuiz(user, quiz).isPresent()) {
            throw new IllegalStateException("이미 푼 퀴즈입니다.");
        }

        boolean isCorrect = quiz.getCorrectAnswer().equals(request.getAnswer());

        UserQuiz userQuiz = UserQuiz.builder()
                .user(user)
                .quiz(quiz)
                .selectedAnswer(request.getAnswer())
                .isCorrect(isCorrect)
                .build();

        userQuizRepository.save(userQuiz);

        boolean scoreUpdated = false;
        Integer newScore = null;

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
}
