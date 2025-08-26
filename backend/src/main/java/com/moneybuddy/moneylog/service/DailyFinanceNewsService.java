package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.FinancialKeyword;
import com.moneybuddy.moneylog.domain.FinancialKnowledge;
import com.moneybuddy.moneylog.repository.FinancialKeywordRepository;
import com.moneybuddy.moneylog.repository.FinancialKnowledgeRepository;
import com.moneybuddy.moneylog.repository.FinancialTitleCacheRepository;
import com.moneybuddy.moneylog.util.KeywordExtractor;
import com.moneybuddy.moneylog.util.TitleDeduper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyFinanceNewsService {

    private final FinancialKnowledgeRepository newsRepo;
    private final FinancialKeywordRepository keywordRepo;
    private final FinancialTitleCacheRepository titleCacheRepo;

    private final KeywordExtractor keywordExtractor;
    private final TitleDeduper titleDeduper;

    // 수집 대상 RSS (원하는 만큼 추가/수정)
    private static final List<String> FEEDS = List.of(
            "https://www.hankyung.com/feed/finance",
            "https://www.hankyung.com/feed/economy",
            "https://www.mk.co.kr/rss/30100041/",
            "https://news.google.com/rss/headlines/section/topic/BUSINESS?hl=ko&gl=KR&ceid=KR:ko"
    );

    // 후보 기사 로딩 메서드
    public List<SyndEntry> loadCandidates(int perFeed) {
        List<SyndEntry> out = new ArrayList<>();
        for (String url : FEEDS) {
            try {
                SyndFeed feed = new SyndFeedInput().build(new XmlReader(new URL(url)));
                feed.getEntries().stream()
                        .limit(perFeed)
                        .forEach(out::add);
            } catch (Exception ignored) {
                // 개별 피드 실패는 무시
            }
        }
        // 발행시각 없는 항목 제거 + 최신순 정렬
        out.removeIf(e -> e.getPublishedDate() == null);
        out.sort(Comparator.comparing(SyndEntry::getPublishedDate).reversed());
        return out;
    }

    /**
     * 오늘 날짜에 중복 제거(키워드/제목 유사도) 후 최대 topK개 저장
     */
    @Transactional
    public int ingestTodayWithDedupe(int perFeed, int topK, int maxPerKeywordPerDay, double titleSimThreshold) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        List<SyndEntry> candidates = loadCandidates(perFeed);
        if (candidates == null) candidates = List.of(); // NPE 방지

        int saved = 0;
        for (SyndEntry e : candidates) {
            if (saved >= topK) break;

            String title = e.getTitle();
            String link  = e.getLink();
            String desc  = (e.getDescription() != null) ? e.getDescription().getValue() : "";
            String plain = org.jsoup.Jsoup.parse(desc).text();

            // 동일 제목(당일) 중복
            if (newsRepo.existsByTitleAndDate(title, today)) continue;

            // 제목 유사도 중복(다른 매체 동일 이슈)
            if (titleDeduper.isNearDuplicate(today, title, titleSimThreshold)) continue;

            // 키워드 추출 및 일일 제한
            List<String> kws = keywordExtractor.extractAndNormalize(title + " " + plain, 5);
            if (kws.isEmpty()) kws = List.of(title.toLowerCase());

            boolean blocked = false;
            for (String kw : kws) {
                if (keywordRepo.countByDateAndKeyword(today, kw) >= maxPerKeywordPerDay) {
                    blocked = true; break;
                }
            }
            if (blocked) continue;

            // 저장
            newsRepo.save(new FinancialKnowledge(title, plain + "\n\n원문: " + link, today));

            // 제목 캐시 저장
            titleDeduper.remember(today, title);

            // 키워드 저장(중복/한도 체크)
            for (String kw : kws) {
                if (keywordRepo.countByDateAndKeyword(today, kw) < maxPerKeywordPerDay) {
                    if (!keywordRepo.existsByDateAndKeyword(today, kw)) {
                        keywordRepo.save(new FinancialKeyword(today, kw));
                    }
                }
            }

            saved++;
        }
        return saved;
    }
}
