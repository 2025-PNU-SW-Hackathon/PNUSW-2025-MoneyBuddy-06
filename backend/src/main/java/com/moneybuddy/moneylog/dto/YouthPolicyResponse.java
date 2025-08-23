package com.moneybuddy.moneylog.dto;

import com.moneybuddy.moneylog.domain.YouthPolicy;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class YouthPolicyResponse {

    private Long id;
    private String title;
    private String applicationPeriod;
    private String amount;
    private String benefit;
    private String eligibility;
    private String applicationMethod;
    private String description;
    private String url;

    public static YouthPolicyResponse fromEntity(YouthPolicy policy) {
        return YouthPolicyResponse.builder()
                .id(policy.getId())
                .title(policy.getTitle())
                .applicationPeriod(policy.getApplicationPeriod())
                .amount(policy.getAmount())
                .benefit(policy.getBenefit())
                .eligibility(policy.getEligibility())
                .applicationMethod(policy.getApplicationMethod())
                .description(policy.getDescription())
                .url(policy.getUrl())
                .build();
    }
}