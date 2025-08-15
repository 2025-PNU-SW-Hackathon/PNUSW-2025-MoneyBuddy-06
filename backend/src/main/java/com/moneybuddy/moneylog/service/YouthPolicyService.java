package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.YouthPolicy;
import com.moneybuddy.moneylog.dto.YouthPolicyResponse;
import com.moneybuddy.moneylog.repository.YouthPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouthPolicyService {

    private final YouthPolicyRepository youthPolicyRepository;

    public List<YouthPolicyResponse> getAllPolicies() {
        List<YouthPolicy> policies = youthPolicyRepository.findAll();

        return policies.stream()
                .map(YouthPolicyResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
