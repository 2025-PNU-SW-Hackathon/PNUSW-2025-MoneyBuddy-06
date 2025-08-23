package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.response.YouthPolicyResponse;
import com.moneybuddy.moneylog.service.YouthPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/youth-policy")
@RequiredArgsConstructor
public class YouthPolicyController {

    private final YouthPolicyService youthPolicyService;

    @GetMapping
    public List<YouthPolicyResponse> getAllPolicies() {
        return youthPolicyService.getAllPolicies();
    }
}
