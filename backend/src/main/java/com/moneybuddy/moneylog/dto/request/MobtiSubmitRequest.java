package com.moneybuddy.moneylog.dto.request;

import lombok.Data;
import java.util.List;

// answer: 길이 20, 각 항목은 O 또는 X (대소문자 무관)
@Data
public class MobtiSubmitRequest {
    private List<String> answers;
}
