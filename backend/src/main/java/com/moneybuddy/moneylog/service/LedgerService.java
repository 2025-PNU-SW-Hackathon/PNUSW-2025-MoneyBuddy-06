package com.moneybuddy.moneylog.service;

import com.moneybuddy.moneylog.domain.EntryType;
import com.moneybuddy.moneylog.dto.request.LedgerRequest;
import com.moneybuddy.moneylog.dto.request.NotificationRequest;
import com.moneybuddy.moneylog.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.domain.Ledger;

import com.moneybuddy.moneylog.model.NotificationAction;
import com.moneybuddy.moneylog.model.NotificationType;
import com.moneybuddy.moneylog.model.TargetType;
import com.moneybuddy.moneylog.port.Notifier;

import com.moneybuddy.moneylog.repository.LedgerRepository;
import com.moneybuddy.moneylog.util.MessageParser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final MessageParser messageParser;

    private final Notifier notifier; // 알림 전송용

    // 자동 작성(인앱 알림/영수증 파싱)
    public LedgerEntryDto parseAndSave(NotificationRequest request) {
        var parsed = messageParser.parseNotification(request.getMessage(), request.getReceivedAt());
        String resolvedAsset = Optional.ofNullable(parsed.getAsset()).orElse("미지정");

        var type = EntryType.EXPENSE;
        BigDecimal signed = parsed.getAmount().abs().negate();

        Ledger ledger = Ledger.builder()
                .userId(request.getUserId())
                .dateTime(parsed.getDateTime())
                .amount(signed)
                .entryType(type)
                .asset(resolvedAsset)
                .store(parsed.getStore())
                .category(parsed.getCategory())
                .createdAt(LocalDateTime.now())
                .build();

        ledgerRepository.save(ledger);

        Long ocrId = ledger.getId();
        notifier.send(
                request.getUserId(),
                NotificationType.LEDGER_REMINDER,
                TargetType.LEDGER,
                ocrId,
                "알림 인식 완료!",
                "인식된 내역으로 작성된 가계부를 확인해 보세요.",
                NotificationAction.OPEN_LEDGER_NEW,
                Map.of("from", "ocr", "ocrId", ocrId),
                "/ledger/new?from=ocr&ocrId=" + ocrId
        );

        return new LedgerEntryDto(ledger);
    }

    // 수동 작성
    public LedgerEntryDto create(Long userId, LedgerRequest req) {
        var type = Objects.requireNonNull(req.getEntryType(), "entryType은 필수입니다.");
        BigDecimal base = Objects.requireNonNull(req.getAmount(), "amount는 필수입니다.").abs();
        BigDecimal signed = (type == EntryType.EXPENSE) ? base.negate() : base;

        // asset: EXPENSE = 필수, INCOME = 기본값
        String asset = req.getAsset();
        if (type == EntryType.EXPENSE) {
            if (asset == null || asset.isBlank()) {
                throw new IllegalArgumentException("지출에서는 asset이 필수입니다.");
            }
            else {
                if (asset == null || asset.isBlank()) {
                    asset = "미지정";
                }
            }
        }

        Ledger ledger = Ledger.builder()
                .userId(userId)
                .dateTime(req.getDateTime())
                .amount(signed)
                .entryType(type)
                .asset(asset)
                .store(req.getStore())  // null로 저장
                .category(req.getCategory())
                .description(req.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        Ledger saved = ledgerRepository.save(ledger);
        return new LedgerEntryDto(saved);
    }

    public LedgerEntryDto update(Long userId, Long id, LedgerRequest req) {
        Ledger ledger = ledgerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("내역이 없습니다. id=" + id));
        if (!ledger.getUserId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }

        var type = Objects.requireNonNull(req.getEntryType(), "entryType은 필수입니다.");
        BigDecimal base = Objects.requireNonNull(req.getAmount(), "amount는 필수입니다.").abs();
        BigDecimal signed = (type == EntryType.EXPENSE) ? base.negate() : base;

        // asset: EXPENSE = 필수, INCOME = 기본값
        String asset = req.getAsset();
        if (type == EntryType.EXPENSE) {
            if (asset == null || asset.isBlank()) {
                throw new IllegalArgumentException("지출에서는 asset이 필수입니다.");
            }
            else {
                if (asset == null || asset.isBlank()) {
                    asset = "미지정";
                }
            }
        }

        ledger.setDateTime(req.getDateTime());
        ledger.setAmount(signed);
        ledger.setEntryType(type);
        ledger.setAsset(asset);
        ledger.setStore(req.getStore());
        ledger.setCategory(req.getCategory());
        ledger.setDescription(req.getDescription());

        return new LedgerEntryDto(ledger);
    }

    public void delete(Long userId, Long id) {
        Ledger ledger = ledgerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("내역이 없습니다. id=" + id));
        if (!ledger.getUserId().equals(userId)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
        ledgerRepository.delete(ledger);
    }
}
