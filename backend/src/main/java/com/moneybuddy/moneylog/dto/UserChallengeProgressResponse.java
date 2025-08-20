package com.moneybuddy.moneylog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserChallengeProgressResponse {
    private List<UserChallengeResponse> inProgress;
    private List<UserChallengeResponse> completed;
}
