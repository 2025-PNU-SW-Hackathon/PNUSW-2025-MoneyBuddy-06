package com.moneybuddy.moneylog.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;

@ToString
@Getter
@Setter
public class ChallengeFilterRequest {
    private String type;

    @JsonAlias({"category", "categories"})
    private Object categoriesRaw;

    private Boolean isAccountLinked;

    public String getCategory() {

        if (categoriesRaw instanceof String s) {
            s = s.trim();
            return s.isEmpty() || "전체".equals(s) || "ALL".equalsIgnoreCase(s) ? null : s;
        }

        if (categoriesRaw instanceof List<?> list) {
            for (Object o : list) {
                if (o == null) continue;
                String s = o.toString().trim();
                if (!s.isEmpty() && !"전체".equals(s) && !"ALL".equalsIgnoreCase(s)) {
                    return s;
                }
            }
            return null;
        }
        return null;
    }
}