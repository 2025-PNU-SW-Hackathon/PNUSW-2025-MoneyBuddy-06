package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.dto.request.QuizAnswerRequest;
import com.moneybuddy.moneylog.dto.response.QuizResponse;
import com.moneybuddy.moneylog.dto.response.QuizResultResponse;
import com.moneybuddy.moneylog.repository.UserRepository;
import com.moneybuddy.moneylog.service.QuizService;
import com.moneybuddy.moneylog.security.CustomUserDetails;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;

    // 오늘의 문제 가져오기
    @GetMapping("/today")
    public QuizResponse getTodayQuiz(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalStateException("유저를 찾을 수 없습니다."));
        return quizService.getUserDailyQuiz(user);
    }

    // 오늘 문제 답 제출
    @PostMapping("/answer")
    public QuizResultResponse submitAnswer(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody QuizAnswerRequest request) {
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalStateException("유저를 찾을 수 없습니다."));
        return quizService.submitAnswer(user, request);
    }

    // 완료한 퀴즈 ID만
    @GetMapping("/me/completed-ids")
    public List<Long> getCompletedIds(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalStateException("유저를 찾을 수 없습니다."));
        return quizService.getCompletedQuizIds(user.getId());
    }

    // 완료한 퀴즈 간단 정보
    @GetMapping("/me/completed")
    public List<QuizResponse> getCompleted(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalStateException("유저를 찾을 수 없습니다."));
        return quizService.getCompletedQuizzes(user.getId());
    }
}
