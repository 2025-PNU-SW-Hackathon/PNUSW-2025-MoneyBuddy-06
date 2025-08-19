package com.moneybuddy.moneylog.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneybuddy.moneylog.entity.Notification;
import com.moneybuddy.moneylog.model.*;
import com.moneybuddy.moneylog.port.Notifier;
import com.moneybuddy.moneylog.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService implements Notifier {

    private final NotificationRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Notifier 구현
    @Override
    @Transactional
    public void send(Long userId,
                     NotificationType type,
                     TargetType targetType,
                     Long targetId,
                     String title,
                     String body,
                     NotificationAction action,
                     Map<String, Object> params,
                     String deeplink) {

        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTargetType(targetType);
        n.setTargetId(targetId);
        n.setTitle(title);
        n.setBody(body);
        n.setAction(action);
        if (params != null && !params.isEmpty()) {
            try {
                n.setParamsJson(objectMapper.writeValueAsString(params));
            }
            catch (Exception e) {
                n.setParamsJson("{}");
            }
        }
        else {
            n.setParamsJson("{}");
        }
        n.setDeeplink(deeplink);
        repository.save(n);
    }

    // 조회, 집계, 읽음
    @Transactional(readOnly = true)
    public NotificationList list(Long userId, Long cursor, int size) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        var rows = repository.findList(userId, threshold, cursor, PageRequest.of(0, size));
        Long nextCursor = rows.size() == size ? rows.get(rows.size() - 1).getId() : null;
        List<NotificationItem> items = rows.stream().map(this::toItem).toList();
        return new NotificationList(items, nextCursor);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        return repository.countByUserIdAndIsReadFalseAndCreatedAtGreaterThanEqual(userId, threshold);
    }

    @Transactional
    public void markRead(Long userId, Long id) {
        Notification n = repository.findById(id).orElseThrow();
        if (!Objects.equals(n.getUserId(), userId)) {
            throw new RuntimeException("Forbidden");
        }
        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
        }
    }

    @Transactional
    public void markAllRead(Long userId) {
        // 최근 30일 목록을 충분히 큰 size로 한 번에 가져와 일괄 처리
        var list = list(userId, null, 500).items();
        list.forEach(i -> markRead(userId, i.id()));
    }

    @Transactional
    public int cleanupExpired() {
        return repository.deleteExpired(LocalDateTime.now());
    }

    private NotificationItem toItem(Notification n) {
        Map<String, Object> params;
        try {
            params = objectMapper.readValue(
                    Optional.ofNullable(n.getParamsJson()).orElse("{}"),
                    new TypeReference<Map<String, Object>>() {}
            );
        }
        catch (Exception e) {
            params = Map.of();
        }
        return new NotificationItem(
                n.getId(),
                n.getType(),
                n.getTitle(),
                n.getBody(),
                n.getCreatedAt(),
                n.isRead(),
                n.getAction(),
                params,
                n.getDeeplink()
        );
    }

    // DTOs
    public record NotificationItem(
            Long id,
            NotificationType type,
            String title,
            String body,
            LocalDateTime createdAt,
            boolean isRead,
            NotificationAction action,
            Map<String, Object> params,
            String deeplink
    ) {}

    public record NotificationList(
            List<NotificationItem> items,
            Long nextCursor
    ) {}
}
