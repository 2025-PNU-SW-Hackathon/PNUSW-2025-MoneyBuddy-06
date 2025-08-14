package com.moneybuddy.moneylog.util;

import com.moneybuddy.moneylog.util.model.ParsedNotification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.regex.*;

@Component
public class MessageParser {

    private static final List<Pattern> PATTERNS = List.of(
            // 신한카드, 우리카드, 현대카드
            Pattern.compile("(\\d{2}/\\d{2})\\s(\\d{2}:\\d{2})\\n([\\d,]+)원\\s(.+)"),

            // 국민카드
            Pattern.compile("승인\\s*(\\d{2}/\\d{2})\\s(\\d{2}:\\d{2})\\s*([\\d,]+)원\\s*(.+)"),

            // 삼성/롯데카드
            Pattern.compile(".*\\n(\\d{2}/\\d{2})\\s(\\d{2}:\\d{2})\\s([\\d,]+)원\\n(.+)")
    );

    private static final List<Map.Entry<Pattern, String>> CARD_ISSUER_RULES = List.of(
            entry("(?i)(kb\\s?국민|국민)카드", "국민카드"),
            entry("(?i)신한\\s?카드", "신한카드"),
            entry("(?i)우리\\s?카드", "우리카드"),
            entry("(?i)현대\\s?카드|hyundai\\s?card", "현대카드"),
            entry("(?i)삼성\\s?카드", "삼성카드"),
            entry("(?i)롯데\\s?카드", "롯데카드"),
            entry("(?i)(nh\\s?농협|농협)\\s?카드", "NH농협카드"),
            entry("(?i)하나\\s?카드", "하나카드"),
            entry("(?i)bc\\s?카드", "BC카드"),
            entry("(?i)(ibk|기업)\\s?카드", "IBK기업카드"),
            entry("(?i)씨티\\s?카드|citibank\\s?card", "씨티카드"),
            entry("(?i)카카오.?뱅크.*카드", "카카오뱅크카드"),
            entry("(?i)토스.?뱅크.*카드", "토스뱅크카드")
    );

    public ParsedNotification parseNotification(String message, LocalDateTime receivedAt) {
        String normalized = message.replace("\r\n", "\n");
        String issuer = detectIssuer(normalized).orElse(null);

        for (Pattern pattern : PATTERNS) {
            Matcher matcher = pattern.matcher(normalized);
            if (matcher.find()) {
                String mmdd = matcher.group(1);
                String hhmm = matcher.group(2);
                String amountStr = matcher.group(3).replace(",", "");
                String store = matcher.group(4).trim();

                BigDecimal amount = new BigDecimal(amountStr);

                // 발생 시각: 수신 시각의 연도를 사용
                LocalDateTime dateTime = toLocalDateTimeUsingYear(receivedAt, mmdd, hhmm);

                String asset = issuer;
                if (asset == null) {
                    asset = "미지정"; // 엔티티 컬럼이 NOT NULL 이므로 기본값 지정
                }

                String category = guessCategory(store);

                return new ParsedNotification(amount, asset, store, category, dateTime);
            }
        }

        throw new IllegalArgumentException("지원되지 않는 카드사 알림 형식입니다.");
    }

    private static Map.Entry<Pattern, String> entry(String regex, String issuer) {
        return new AbstractMap.SimpleEntry<>(Pattern.compile(regex), issuer);
    }

    private Optional<String> detectIssuer(String text) {
        for (var rule : CARD_ISSUER_RULES) {
            if (rule.getKey().matcher(text).find()) {
                return Optional.of(rule.getValue());
            }
        }
        // 대괄호 헤더 스타일 [신한카드], [KB국민카드] 등 일반화 탐지
        Matcher m = Pattern.compile("^\\s*\\[\\s*([가-힣A-Za-z\\s]+?카드)\\s*\\]\\s*", Pattern.MULTILINE).matcher(text);
        if (m.find()) return Optional.of(m.group(1).trim());
        return Optional.empty();
    }

    private LocalDateTime toLocalDateTimeUsingYear(LocalDateTime receivedAt, String mmdd, String hhmm) {
        // mmdd = "07/21"
        String[] mds = mmdd.split("/");
        // hhmm = "12:34"
        String[] hms = hhmm.split(":");
        int year = (receivedAt != null ? receivedAt.getYear() : Year.now().getValue());
        return LocalDateTime.of(
                year,
                Integer.parseInt(mds[0]),
                Integer.parseInt(mds[1]),
                Integer.parseInt(hms[0]),
                Integer.parseInt(hms[1])
        );
    }

    private String guessCategory(String store) {
        if (store.contains("CU") || store.contains("GS25") || store.contains("마트") ||
                store.contains("스타벅스") || store.contains("커피") || store.contains("카페")) {
            return "식사";
        } else if (store.contains("버스") || store.contains("지하철") || store.contains("택시")) {
            return "교통";
        }
        else if (store.contains("롯데시네마") || store.contains("메가박스") || store.contains("CGV")) {
            return "문화여가";
        }
        else if (store.contains("병원") || store.contains("의원") || store.contains("치과") || store.contains("이비인후과")) {
            return "의료건강";
        }
        else if (store.contains("옷") || store.contains("올리브영")) {
            return "의류";
        }
        return "기타";
    }
}
