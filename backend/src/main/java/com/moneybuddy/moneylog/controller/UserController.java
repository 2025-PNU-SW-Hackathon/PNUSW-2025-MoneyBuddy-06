package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.dto.ScoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/score")
    public ResponseEntity<ScoreResponse> getScore(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(
                ScoreResponse.builder()
                        .score(user.getScore())
                        .build()
        );
    }
}
