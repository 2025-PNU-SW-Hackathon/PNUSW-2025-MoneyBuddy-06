package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.LedgerEntryDto;
import com.moneybuddy.moneylog.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/receipt")
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping("/ocr")
    public ResponseEntity<LedgerEntryDto> uploadReceipt(@RequestParam("file") MultipartFile file) {
        Long userId = com.moneybuddy.moneylog.security.SecurityUtils.currentUserId(); // ⬅️ JWT에서 추출
        LedgerEntryDto result = receiptService.processReceipt(file, userId);
        return ResponseEntity.ok(result);
    }
}
