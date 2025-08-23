package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.MobtiInfo;
import com.moneybuddy.moneylog.domain.User;
import com.moneybuddy.moneylog.dto.MobtiBriefDto;
import com.moneybuddy.moneylog.dto.MobtiFullDto;
import com.moneybuddy.moneylog.dto.MobtiResultDto;
import com.moneybuddy.moneylog.dto.request.MobtiSubmitRequest;
import com.moneybuddy.moneylog.dto.UserProfileDto;
import com.moneybuddy.moneylog.repository.MobtiInfoRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobtiService {

    private final UserRepository userRepository;
    private final MobtiInfoRepository mobtiInfoRepository;

    // 유형 4쌍
    private enum Dim { IE, MS, TC, PR }

    private enum Side { I, E, M, S, T, C, P, R }

    // 각 문항에 대한 규칙: 어느 유형의 쌍인지, O면 어느 쪽, X면 어느 쪽
    private record Rule(Dim dim, Side oSide, Side xSide) {}

    private static final Rule[] RULES = new Rule[] {
            // 1. 비싸더라도 내가 좋아하는 브랜드라면 구매한다. O:I / X:E
            new Rule(Dim.IE, Side.I, Side.E),
            // 2. 목표 잔고 확인이 더 즐거움. O:M / X:S
            new Rule(Dim.MS, Side.M, Side.S),
            // 3. 유행 관심 많다. O:T / X:C
            new Rule(Dim.TC, Side.T, Side.C),
            // 4. 큰 소비 전 비교검색. O:P / X:R
            new Rule(Dim.PR, Side.P, Side.R),
            // 5. 작은 사치 필요. O:I / X:E
            new Rule(Dim.IE, Side.I, Side.E),
            // 6. 수익 생기면 저축/이체 습관. O:M / X:S
            new Rule(Dim.MS, Side.M, Side.S),
            // 7. 트렌드보다 내 스타일. O:C / X:T
            new Rule(Dim.TC, Side.C, Side.T),
            // 8. 즉흥 소비. O:R / X:P
            new Rule(Dim.PR, Side.R, Side.P),
            // 9. 비싼 음식 참지 못함. O:I / X:E
            new Rule(Dim.IE, Side.I, Side.E),
            // 10. 꼭 필요 아니면 참음. O:M / X:S
            new Rule(Dim.MS, Side.M, Side.S),
            // 11. 믿브랜드 있음. O:C / X:T
            new Rule(Dim.TC, Side.C, Side.T),
            // 12. 이번 달 예산 미리 세움. O:P / X:R
            new Rule(Dim.PR, Side.P, Side.R),
            // 13. 같은 기능이면 더 저렴. O:E / X:I
            new Rule(Dim.IE, Side.E, Side.I),
            // 14. "돈은 쓰라고~" 공감. O:S / X:M
            new Rule(Dim.MS, Side.S, Side.M),
            // 15. 유행보다 검증 선호. O:C / X:T
            new Rule(Dim.TC, Side.C, Side.T),
            // 16. 기분/분위기에 따라 소비 변동. O:R / X:P
            new Rule(Dim.PR, Side.R, Side.P),
            // 17. 필요 먼저 생각. O:E / X:I
            new Rule(Dim.IE, Side.E, Side.I),
            // 18. 편리함 위해 돈 더 씀. O:S / X:M
            new Rule(Dim.MS, Side.S, Side.M),
            // 19. 추천이 구매에 영향. O:T / X:C
            new Rule(Dim.TC, Side.T, Side.C),
            // 20. 할인/이벤트에 이끌림. O:R / X:P
            new Rule(Dim.PR, Side.R, Side.P)
    };

    @Transactional
    public MobtiResultDto calculateSaveAndReturn(Long userId, MobtiSubmitRequest req) {
        if (req.getAnswers() == null || req.getAnswers().size() != 20) {
            throw new IllegalArgumentException("answers는 길이 20의 배열이어야 합니다.");
        }

        Map<Side, Integer> cnt = new EnumMap<>(Side.class);
        for (Side s : Side.values()) cnt.put(s, 0);

        final List<String> answers = req.getAnswers();
        for (int i = 0; i < 20; i++) {
            String a = answers.get(i);
            if (a == null) throw new IllegalArgumentException("answers[" + i + "] 값이 null입니다.");
            String norm = a.trim().toUpperCase(Locale.ROOT);
            if (!norm.equals("O") && !norm.equals("X")) {
                throw new IllegalArgumentException("answers[" + i + "]는 O 또는 X여야 합니다.");
            }

            Rule r = RULES[i];
            Side chosen = norm.equals("O") ? r.oSide() : r.xSide();
            cnt.put(chosen, cnt.get(chosen) + 1);
        }

        // 각 유형별 최종 선택
        Side ie = decide(cnt.getOrDefault(Side.I,0), cnt.getOrDefault(Side.E,0), Dim.IE);
        Side ms = decide(cnt.getOrDefault(Side.M,0), cnt.getOrDefault(Side.S,0), Dim.MS);
        Side tc = decide(cnt.getOrDefault(Side.T,0), cnt.getOrDefault(Side.C,0), Dim.TC);
        Side pr = decide(cnt.getOrDefault(Side.P,0), cnt.getOrDefault(Side.R,0), Dim.PR);

        String code = "" + ie + ms + tc + pr;

        // 저장
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        user.setMobti(code);
        user.setMobtiUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // 응답 DTO
        Map<String,Integer> outCounts = new LinkedHashMap<>();
        outCounts.put("I", cnt.getOrDefault(Side.I,0));
        outCounts.put("E", cnt.getOrDefault(Side.E,0));
        outCounts.put("M", cnt.getOrDefault(Side.M,0));
        outCounts.put("S", cnt.getOrDefault(Side.S,0));
        outCounts.put("T", cnt.getOrDefault(Side.T,0));
        outCounts.put("C", cnt.getOrDefault(Side.C,0));
        outCounts.put("P", cnt.getOrDefault(Side.P,0));
        outCounts.put("R", cnt.getOrDefault(Side.R,0));

        return new MobtiResultDto(
                user.getId(),
                user.getEmail(),
                code,
                outCounts,
                user.getMobtiUpdatedAt()
        );
    }

    private Side decide(int aCount, int bCount, Dim dim) {
        if (aCount > bCount) {
            return switch (dim) {
                case IE -> Side.I;
                case MS -> Side.M;
                case TC -> Side.T;
                case PR -> Side.P;
            };
        } else {
            return switch (dim) {
                case IE -> Side.E;
                case MS -> Side.S;
                case TC -> Side.C;
                case PR -> Side.R;
            };
        }
    }

    // 프로필 조회 등에 사용
    @Transactional(readOnly = true)
    public UserProfileDto loadProfile(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        return new UserProfileDto(u.getId(), u.getEmail(), u.getMobti(), u.getMobtiUpdatedAt());
    }

    // mobti 요약(3개 필드) 반환
    @Transactional(readOnly = true)
    public MobtiBriefDto getMyMobtiBrief(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        if (u.getMobti() == null || u.getMobti().isBlank()) {
            throw new IllegalStateException("사용자의 MOBTI 코드가 아직 없습니다.");
        }
        MobtiInfo info = mobtiInfoRepository.findById(u.getMobti())
                .orElseThrow(() -> new IllegalStateException("mobti 사전에 코드가 없습니다: " + u.getMobti()));
        return new MobtiBriefDto(info.getCode(), info.getNickname(), info.getSummaryShort());
    }

    // mobti 상세(4개 필드) 반환
    @Transactional(readOnly = true)
    public MobtiFullDto getMyMobtiFull(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        if (u.getMobti() == null || u.getMobti().isBlank()) {
            throw new IllegalStateException("사용자의 MOBTI 코드가 아직 없습니다.");
        }
        MobtiInfo info = mobtiInfoRepository.findById(u.getMobti())
                .orElseThrow(() -> new IllegalStateException("mobti 사전에 코드가 없습니다: " + u.getMobti()));

        return new MobtiFullDto(
                info.getCode(),
                info.getNickname(),
                info.getSummaryShort(),
                toLines(info.getDetailTraits()),
                toLines(info.getSpendingTendency()),
                toLines(info.getSocialStyle())
        );
    }

    private static List<String> toLines(String text) {
        if (text == null) return List.of();
        return Arrays.stream(text.split("\\r?\\n"))
                .map(s -> s.replaceFirst("^\\s*[-•]\\s*", "")) // 맨 앞 불릿 제거
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
