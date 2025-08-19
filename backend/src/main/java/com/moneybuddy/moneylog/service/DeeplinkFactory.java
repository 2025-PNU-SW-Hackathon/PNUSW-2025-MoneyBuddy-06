package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.model.NotificationAction;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Component
public class DeeplinkFactory {
    public String build(NotificationAction action, Map<String, Object> params) {
        return switch (action) {
            case OPEN_CHALLENGE_DETAIL -> "/challenges/" + params.get("challengeId");
            case OPEN_LEDGER_NEW -> UriComponentsBuilder.fromPath("/ledger/new")
                    .queryParam("from", params.getOrDefault("from", "ocr"))
                    .queryParam("ocrId", params.get("ocrId"))
                    .toUriString();
            case OPEN_SPENDING_STATS -> "/stats/spending";
            case OPEN_QUIZ_TODAY -> "/quiz/today";
            case OPEN_FINANCE_ARTICLE -> "/finance/" + params.get("articleId");
            case OPEN_PROFILE_LEVEL -> "/profile/level";
        };
    }
}
