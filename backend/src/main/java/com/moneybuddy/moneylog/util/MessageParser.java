package com.moneybuddy.moneylog.util;

import com.moneybuddy.moneylog.model.ParsedNotification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
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

    private static final LinkedHashMap<String, List<Pattern>> CATEGORY_RULES = new LinkedHashMap<>();

    static {
        CATEGORY_RULES.put("식비", pats(
                "CU", "GS25", "세븐일레븐|7\\s*-?\\s*Eleven", "이마트24", "편의점",
                "마트|수퍼|슈퍼|시장|식자재", "홈플러스", "이마트(?!24)", "롯데마트", "코스트코", "하나로|농협",
                "배달의민족|요기요|쿠팡이츠",
                "식당|분식|한식|중식|일식|족발|보쌈|치킨|피자|버거|" +
                        "맥도날드|버거킹|롯데리아|서브웨이|KFC|포차|호프|술집|바"
        ));

        CATEGORY_RULES.put("카페베이커리", pats(
                "스타벅스", "투썸", "이디야", "파스쿠찌", "빽다방", "할리스", "컴포즈",
                "커피", "카페",
                "파리바게뜨", "뚜레쥬르", "베이커리|빵집"
        ));

        CATEGORY_RULES.put("교통", pats(
                "버스", "지하철", "택시", "카카오\\s*T",
                "KTX", "SRT", "코레일",
                "티머니|Tmoney", "고속버스|시외버스|대중교통"
        ));

        CATEGORY_RULES.put("문화여가", pats(
                "롯데시네마", "메가박스", "CGV", "영화관|영화",
                "공연|전시|뮤지컬|콘서트",
                "넷플릭스|Netflix", "디즈니\\+|Disney",
                "유튜브\\s*프리미엄|YouTube\\s*Premium",
                "멜론|지니뮤직|티빙|웨이브|쿠팡플레이"
        ));

        CATEGORY_RULES.put("의료건강", pats(
                "병원", "의원", "치과", "이비인후과", "피부과",
                "내과|외과|정형외과|산부인과|비뇨의학과",
                "약국", "한의원|한약"
        ));

        CATEGORY_RULES.put("의류미용", pats(
                "옷", "의류|패션", "무신사|지그재그|에이블리",
                "올리브영|에뛰드|이니스프리", "화장품",
                "미용실|헤어|커트|펌|파마|염색",
                "네일|왁싱|메이크업"
        ));
    }

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

    private static List<Pattern> pats(String... regexes) {
        return Arrays.stream(regexes)
                .map(r -> Pattern.compile(r, Pattern.CASE_INSENSITIVE))
                .toList();
    }

    private static String normalize(String src) {
        if (src == null) return "";
        String s = Normalizer.normalize(src, Normalizer.Form.NFKC); // 전각/반각 등 통일
        s = s.replaceAll("\\s+", " ").trim();   // 공백 정리
        return s;
    }

    private String guessCategory(String store) {
        String s = normalize(store);

        for (Map.Entry<String, List<Pattern>> e : CATEGORY_RULES.entrySet()) {
            for (Pattern p : e.getValue()) {
                if (p.matcher(s).find()) {
                    return e.getKey();
                }
            }
        }
        return "기타";
    }
}
