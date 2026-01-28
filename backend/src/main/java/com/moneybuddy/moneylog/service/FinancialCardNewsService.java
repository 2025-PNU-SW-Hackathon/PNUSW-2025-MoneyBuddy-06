package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.FinancialCardNews;
import com.moneybuddy.moneylog.dto.response.FinancialCardNewsResponse;
import com.moneybuddy.moneylog.repository.FinancialCardNewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialCardNewsService {

    private final FinancialCardNewsRepository repository;

    // 오늘 날짜의 카드뉴스를 모두 조회하고, KnowledgeResponse 리스트로 변환하여 반환
    public List<FinancialCardNewsResponse> getTodayKnowledgeList() {
        // 오늘 날짜
        var KST = java.time.ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now();

        // 오늘 날짜의 카드뉴스 전부 가져오기
        List<FinancialCardNews> list = repository.findAllByDate(today);

        // 결과가 없을 경우 예외 처리
        if (list.isEmpty()) {
            throw new IllegalArgumentException("오늘의 카드뉴스가 없습니다.");
        }

        // 각 카드뉴스를 DTO로 변환하여 리스트로 반환
        return list.stream()
                .map(k -> new FinancialCardNewsResponse(k.getTitle(), k.getContent(), k.getDate()))

                .toList();
    }
}