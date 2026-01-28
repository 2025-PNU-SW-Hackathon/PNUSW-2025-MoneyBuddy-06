package com.moneybuddy.moneylog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.moneybuddy.moneylog.client.ClovaReceiptOcrClient;
import com.moneybuddy.moneylog.domain.EntryType;
import com.moneybuddy.moneylog.domain.Ledger;
import com.moneybuddy.moneylog.dto.LedgerEntryDto;
import com.moneybuddy.moneylog.model.NotificationAction;
import com.moneybuddy.moneylog.model.NotificationType;
import com.moneybuddy.moneylog.model.TargetType;
import com.moneybuddy.moneylog.port.Notifier;
import com.moneybuddy.moneylog.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.*;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ClovaReceiptOcrClient clova;
    private final LedgerRepository ledgerRepository;
    private final Notifier notifier; // 알림 전송용

    private final BudgetWarningService budgetWarningService;   // 목표 금액 초과 여부 검사용 서비스

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

    public LedgerEntryDto processReceipt(MultipartFile file, Long userId) {
        JsonNode root = clova.requestReceiptOcr(file);

        JsonNode image = root.path("images").path(0);
        if (!"SUCCESS".equalsIgnoreCase(image.path("inferResult").asText())) {
            String msg = image.path("message").asText("");
            throw new IllegalStateException("영수증 인식 실패: " + msg);
        }

        JsonNode result = image.path("receipt").path("result");

        // 매장명
        String store = getValue(result.path("storeInfo").path("name"));

        // 결제일시
        LocalDate date = parseDate(result.path("paymentInfo").path("date"));
        LocalTime time = parseTime(result.path("paymentInfo").path("time"));
        LocalDateTime paidAt = (date != null && time != null)
                ? LocalDateTime.of(date, time)
                : (date != null ? date.atStartOfDay() : LocalDateTime.now(ZoneId.of("Asia/Seoul")));

        // 자산(카드사)
        String asset = getValue(result.path("paymentInfo").path("cardInfo").path("company"));
        if (asset == null || asset.isBlank()) asset = "미지정";

        // 총 결제 금액 (카드번호/승인번호 등과 혼동 방지: totalPrice.price 만 사용)
        BigDecimal total = parseMoney(result.path("totalPrice").path("price"));
        if (total == null) {
            // 총액 누락 시 품목 합으로 보정
            total = sumItems(result);
        }
        if (total == null) {
            throw new IllegalStateException("총 결제 금액을 인식하지 못했습니다.");
        }

        // Ledger 저장: 지출은 음수
        BigDecimal addedAbs = total.abs();
        BigDecimal signed = addedAbs.negate();
        Ledger ledger = Ledger.builder()
                .userId(userId)
                .dateTime(paidAt)
                .amount(signed)
                .entryType(EntryType.EXPENSE)
                .asset(asset)
                .store(store)
                .category(guessCategory(store))
                .createdAt(LocalDateTime.now())
                .build();
        ledgerRepository.save(ledger);

        Long ocrId = ledger.getId();

        notifier.send(
                userId,
                NotificationType.LEDGER_REMINDER,
                TargetType.LEDGER,
                ocrId,
                "영수증 인식 완료!",
                "인식된 내역으로 작성된 가계부를 확인해 보세요.",
                NotificationAction.OPEN_LEDGER_NEW,
                Map.of("from", "ocr", "ocrId", ocrId),
                null
        );

        // 저장 직후 -> 월 목표 금액 초과 여부 검사
        budgetWarningService.checkAndNotifyOnExpense(userId, paidAt, addedAbs);

        return new LedgerEntryDto(ledger);
    }

    private String getValue(JsonNode node) {
        // formatted.value 우선, 없으면 text
        String f = node.path("formatted").path("value").asText(null);
        if (f != null && !f.isBlank()) return f;
        String t = node.path("text").asText(null);
        return (t != null && !t.isBlank()) ? t : null;
    }

    private BigDecimal parseMoney(JsonNode priceNode) {
        String s = getValue(priceNode);
        if (s == null) return null;
        // "￦12,300", "12,300원" 등에서 숫자만 추출
        String cleaned = s.replaceAll("[^\\d\\.-]", "").replace(",", "");
        if (cleaned.isBlank() || cleaned.equals("-")) return null;
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseDate(JsonNode dateNode) {
        if (dateNode.isMissingNode()) return null;
        JsonNode f = dateNode.path("formatted");
        String y = f.path("year").asText(null);
        String m = f.path("month").asText(null);
        String d = f.path("day").asText(null);
        if (y == null || m == null || d == null) return null;
        return LocalDate.of(Integer.parseInt(y), Integer.parseInt(m), Integer.parseInt(d));
    }

    private LocalTime parseTime(JsonNode timeNode) {
        if (timeNode.isMissingNode()) return null;
        JsonNode f = timeNode.path("formatted");
        String hh = f.path("hour").asText(null);
        String mm = f.path("minute").asText(null);
        String ss = f.path("second").asText(null);
        if (hh == null || mm == null) return null;
        int h = Integer.parseInt(hh), m = Integer.parseInt(mm), s = (ss != null ? Integer.parseInt(ss) : 0);
        return LocalTime.of(h, m, s);
    }

    private BigDecimal sumItems(JsonNode result) {
        BigDecimal sum = BigDecimal.ZERO;
        boolean found = false;
        JsonNode subResults = result.path("subResults");
        if (subResults.isArray()) {
            for (JsonNode group : subResults) {
                JsonNode items = group.path("items");
                if (items.isArray()) {
                    for (JsonNode it : items) {
                        BigDecimal price = parseMoney(it.path("price").path("price"));
                        if (price != null) { sum = sum.add(price); found = true; }
                    }
                }
            }
        }
        return found ? sum : null;
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
