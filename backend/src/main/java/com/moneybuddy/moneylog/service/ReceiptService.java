package com.moneybuddy.moneylog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneybuddy.moneylog.client.ClovaOcrClient;
import com.moneybuddy.moneylog.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.domain.Ledger;
import com.moneybuddy.moneylog.repository.LedgerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ClovaOcrClient clovaOcrClient;
    private final LedgerRepository ledgerRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LedgerEntryDto processReceipt(MultipartFile file, Long userId) {
        // OCR 호출
        String ocrResponse = clovaOcrClient.callOcrApi(file);

        // JSON 파싱
        StringBuilder sb = new StringBuilder();
        try {
            JsonNode root = objectMapper.readTree(ocrResponse);
            JsonNode fields = root.path("images").get(0).path("fields");

            for (JsonNode field : fields) {
                String inferText = field.path("inferText").asText();
                sb.append(inferText).append(" ");
            }
        } catch (Exception e) {
            throw new RuntimeException("OCR 응답 파싱 실패", e);
        }

        String extractedText = sb.toString();

        BigDecimal amount = extractAmount(extractedText); // 금액(양수) 추출
        String store = extractStore(extractedText);       // 상호명 추출(임시)
        String category = guessCategory(store);

        BigDecimal signedAmount = amount.abs().negate();

        Ledger ledger = Ledger.builder()
                .userId(userId)
                .amount(signedAmount)          // ← 음수로 저장
                .store(store)
                .category(category)
                .dateTime(LocalDateTime.now()) // OCR에 날짜가 있으면 교체 가능
                .createdAt(LocalDateTime.now())
                .build();

        ledgerRepository.save(ledger);

        return new LedgerEntryDto(ledger);
    }

    private BigDecimal extractAmount(String text) {
        Pattern pattern = Pattern.compile("(총 ?결제 ?금액|합계|결제금액)[^\\d]*([\\d,]+)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String amountStr = matcher.group(2).replace(",", "");
            return new BigDecimal(amountStr);  // ★ BigDecimal 반환
        }

        matcher = Pattern.compile("([\\d,]+)[원₩]?").matcher(text);
        if (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            return new BigDecimal(amountStr);  // ★ BigDecimal 반환
        }

        throw new IllegalArgumentException("금액을 추출할 수 없습니다.");
    }

    private String extractStore(String text) {
        return text.split(" ")[0]; // 임시로 첫 단어 사용
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
