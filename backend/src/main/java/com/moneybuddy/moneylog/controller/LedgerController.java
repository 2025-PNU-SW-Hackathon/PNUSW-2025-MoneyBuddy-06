package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.dto.request.LedgerRequest;
import com.moneybuddy.moneylog.dto.request.NotificationRequest;
import com.moneybuddy.moneylog.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.service.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    // 자동 작성
    @PostMapping("/auto")
    public ResponseEntity<?> autoCreateLedger(@RequestBody NotificationRequest request) {
        // 1) JWT에서 userId 추출 2) 실패 시 바디의 userId로 대체
        Long userId = resolveUserId(request.getUserId());
        request.setUserId(userId);

        LedgerEntryDto dto = ledgerService.parseAndSave(request);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "parsed", dto
        ));
    }

    // 수동 작성
    // 생성
    @PostMapping
    public ResponseEntity<?> create(@RequestBody LedgerRequest req) {
        Long userId = resolveUserId(null);
        LedgerEntryDto dto = ledgerService.create(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "success",
                "entry", dto
        ));
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody LedgerRequest req) {
        Long userId = resolveUserId(null);
        LedgerEntryDto dto = ledgerService.update(userId, id, req);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "entry", dto
        ));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Long userId = resolveUserId(null);
        ledgerService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(Long fallback) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();

            // 1) 가능한 경우 SecurityContext(JWT 등)에서 userId를 추출
            // (예) 커스텀 필터가 principal에 {userId: 123} 형태의 Map을 넣은 경우
            if (principal instanceof java.util.Map<?,?> map && map.get("userId") != null) {
                return toLong(map.get("userId"));
            }
        }

        // 2) 실패하면 요청 바디 사용
        if (fallback != null) return fallback;
        throw new AccessDeniedException("로그인이 필요합니다.");
    }

    private Long toLong(Object v) {
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        return Long.parseLong(v.toString());
    }
}
