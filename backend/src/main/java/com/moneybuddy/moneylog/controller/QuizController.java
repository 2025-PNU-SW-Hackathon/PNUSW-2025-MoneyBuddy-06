package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.dto.QuizAnswerRequest;
import com.moneybuddy.moneylog.dto.QuizResponse;
import com.moneybuddy.moneylog.dto.QuizResultResponse;
import com.moneybuddy.moneylog.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/today")
    public ResponseEntity<QuizResponse> getTodayQuiz(@RequestAttribute("user") User user) {
        QuizResponse quiz = quizService.getTodayQuiz(user);
        return ResponseEntity.ok(quiz);
    }
    
    @PostMapping("/answer")
    public ResponseEntity<QuizResultResponse> submitAnswer(
            @RequestAttribute("user") User user,
            @RequestBody QuizAnswerRequest request) {

        QuizResultResponse result = quizService.submitAnswer(user, request);
        return ResponseEntity.ok(result);
    }
}
