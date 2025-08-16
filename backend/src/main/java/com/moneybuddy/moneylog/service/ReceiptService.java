package com.moneybuddy.moneylog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.moneybuddy.moneylog.client.ClovaReceiptOcrClient;
import com.moneybuddy.moneylog.domain.EntryType;
import com.moneybuddy.moneylog.domain.Ledger;
import com.moneybuddy.moneylog.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.time.*;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ClovaReceiptOcrClient clova;
    private final LedgerRepository ledgerRepository;

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
        BigDecimal signed = total.abs().negate();
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

    private String guessCategory(String store) {
        if (store.contains("CU") || store.contains("GS25") || store.contains("마트")) {
            return "식비";
        } else if (store.contains("스타벅스") || store.contains("커피") || store.contains("카페")) {
            return "카페/베이커리";
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
            return "의류/미용";
        }
        return "기타";
    }
}
