package com.moneybuddy.moneylog.util;

import com.moneybuddy.moneylog.domain.FinancialTitleCache;
import com.moneybuddy.moneylog.repository.FinancialTitleCacheRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TitleDeduper {

    private final FinancialTitleCacheRepository cacheRepo;

    // 공백 기준 토큰 집합 생성(소문자, 기호 제거)
    private static Set<String> tokenizeTitle(String title) {
        String t = title == null ? "" : title.toLowerCase().replaceAll("[^0-9a-z가-힣\\s]", " ");
        return Arrays.stream(t.split("\\s+"))
                .filter(s -> s.length() >= 2)
                .collect(Collectors.toSet());
    }

    // Jaccard 유사도 계산
    private static double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() || b.isEmpty()) return 0.0;
        Set<String> inter = new HashSet<>(a); inter.retainAll(b);
        Set<String> union = new HashSet<>(a); union.addAll(b);
        return (double) inter.size() / (double) union.size();
    }

    // 오늘 이미 저장된 제목들과 비슷한지 검사
    public boolean isNearDuplicate(LocalDate date, String newTitle, double threshold) {
        Set<String> tNew = tokenizeTitle(newTitle);
        for (FinancialTitleCache c : cacheRepo.findAllByDate(date)) {
            if (jaccard(tNew, tokenizeTitle(c.getTitle())) >= threshold) {
                return true;
            }
        }
        return false;
    }

    // 새 제목을 캐시에 기록
    @Transactional
    public void remember(LocalDate date, String title) {
        cacheRepo.save(new FinancialTitleCache(date, title));
    }
}
