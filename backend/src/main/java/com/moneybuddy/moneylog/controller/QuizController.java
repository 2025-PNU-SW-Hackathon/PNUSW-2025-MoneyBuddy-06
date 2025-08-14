package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.dto.QuizAnswerRequest;
import com.moneybuddy.moneylog.dto.QuizResponse;
import com.moneybuddy.moneylog.dto.QuizResultResponse;
import com.moneybuddy.moneylog.repository.UserRepository;
import com.moneybuddy.moneylog.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.moneybuddy.moneylog.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository; // 추가 필요

    @GetMapping("/today")
    public ResponseEntity<QuizResponse> getTodayQuiz(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // User 엔티티를 DB에서 조회
        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        QuizResponse quiz = quizService.getTodayQuiz(user);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/answer")
    public ResponseEntity<QuizResultResponse> submitAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody QuizAnswerRequest request) {

        User user = userRepository.findById(userDetails.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        QuizResultResponse result = quizService.submitAnswer(user, request);
        return ResponseEntity.ok(result);
    }
}
