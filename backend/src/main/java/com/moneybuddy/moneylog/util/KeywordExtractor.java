package com.moneybuddy.moneylog.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class KeywordExtractor {

    // 간단 동의어 사전: 같은 이슈를 한 키워드로 수렴
    private static final Map<String, String> SYNONYM = Map.ofEntries(
            Map.entry("금리", "기준금리"),
            Map.entry("기준금리", "기준금리"),
            Map.entry("관세", "관세"),
            Map.entry("관세율", "관세"),
            Map.entry("세제", "세제"),
            Map.entry("증세", "세제"),
            Map.entry("양도세", "세제"),
            Map.entry("배당세", "세제"),
            Map.entry("코스피", "코스피"),
            Map.entry("코스닥", "코스닥"),
            Map.entry("인하", "인하"),
            Map.entry("동결", "동결"),
            Map.entry("환율", "환율")
            // 프로젝트 상황에 맞춰 계속 보강
    );

    private static final Set<String> STOPWORDS = Set.of(
            "그리고","그러나","하지만","또한","관련","대한","통해","대해","오늘","이번","지난",
            "정부","발표","경제","금융","시장","기사","속보","뉴스","종합","한국","대한민국",
            "the","a","an","and","or","for","of","to","in","on","at","by","with"
    );

    // 제목·본문에서 상위 N개 키워드 추출 후 정규화
    public List<String> extractAndNormalize(String text, int topN) {
        if (text == null) return List.of();

        // 소문자화 + 비문자 제거(한글/영문/숫자/공백만 남김)
        String normalized = text.toLowerCase().replaceAll("[^0-9a-z가-힣\\s]", " ");
        String[] tokens = normalized.split("\\s+");

        Map<String, Integer> freq = new HashMap<>();
        for (String t : tokens) {
            if (t.length() < 2) continue;
            if (STOPWORDS.contains(t)) continue;

            // 동의어 수렴: 사전에 있으면 대표어로 치환
            String canon = SYNONYM.getOrDefault(t, t);
            freq.merge(canon, 1, Integer::sum);
        }

        return freq.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .toList();
    }
}
