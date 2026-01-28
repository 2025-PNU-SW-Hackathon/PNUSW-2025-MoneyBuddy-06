package com.moneybuddy.moneylog.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.moneybuddy.moneylog.domain.UserDeviceToken;
import com.moneybuddy.moneylog.model.*;
import com.moneybuddy.moneylog.port.Notifier;
import com.moneybuddy.moneylog.repository.NotificationRepository;
import com.moneybuddy.moneylog.repository.UserDeviceTokenRepository;
import com.moneybuddy.moneylog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService implements Notifier {

    private final NotificationRepository repository;
    private final UserDeviceTokenRepository tokenRepository;
    private final UserRepository userRepository;

    // FirebaseMessaging 주입, 설정 없으면 null
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    private final DeeplinkFactory deeplinkFactory;
    private final ObjectMapper objectMapper;

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

        // deeplink 자동 생성
        Map<String, Object> safeParams = (params != null ? params : Map.of());
        String effectiveDeeplink = (deeplink == null || deeplink.isBlank())
                ? deeplinkFactory.build(action, safeParams)
                : deeplink;

        com.moneybuddy.moneylog.domain.Notification n = new com.moneybuddy.moneylog.domain.Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTargetType(targetType);
        n.setTargetId(targetId);
        n.setTitle(title);
        n.setBody(body);
        n.setAction(action);
        try {
            n.setParamsJson(objectMapper.writeValueAsString(params != null ? params : Map.of()));
        } catch (Exception e) {
            n.setParamsJson("{}");
        }
        n.setDeeplink(effectiveDeeplink);
        repository.save(n);

        // 사용자 푸시 설정 확인 (off면 발송 생략)
        var user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isNotificationEnabled()) return;

        // FCM 푸시 발송
        FirebaseMessaging fm = firebaseMessagingProvider.getIfAvailable();
        if (fm == null) {
            // FCM 미설정이면 푸시 생략 (앱은 정상 동작)
            return;
        }

        List<UserDeviceToken> tokens = tokenRepository.findByUserIdAndEnabledTrueAndReauthRequiredFalse(userId);
        if (tokens.isEmpty()) return;

        List<String> tokenStrings = tokens.stream()
                .map(UserDeviceToken::getDeviceToken)
                .filter(s -> s != null && !s.isBlank())
                .toList();
        if (tokenStrings.isEmpty()) return;

        // data payload에 라우팅 정보 같이 전달 (클릭 시 deeplink 사용)
        Map<String, String> data = new HashMap<>();
        data.put("type", type.name());
        data.put("action", action.name());
        data.put("deeplink", effectiveDeeplink != null ? effectiveDeeplink : "");
        if (params != null && !params.isEmpty()) {
            try {
                data.put("params", objectMapper.writeValueAsString(params));
            } catch (Exception ignored) {}
        }
        if (targetType != null) data.put("targetType", targetType.name());
        if (targetId != null) data.put("targetId", String.valueOf(targetId));
        data.put("notificationId", String.valueOf(n.getId())); // DB에 저장된 알림 id

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokenStrings)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        try {
            BatchResponse resp = fm.sendEachForMulticast(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        com.moneybuddy.moneylog.domain.Notification n = repository.findById(id).orElseThrow();
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
        var list = list(userId, null, 500).items();
        list.forEach(i -> markRead(userId, i.id()));
    }

    @Transactional
    public int cleanupExpired() {
        return repository.deleteExpired(LocalDateTime.now());
    }

    private NotificationItem toItem(com.moneybuddy.moneylog.domain.Notification n) {
        Map<String, Object> params;
        try {
            params = objectMapper.readValue(
                    Optional.ofNullable(n.getParamsJson()).orElse("{}"),
                    new TypeReference<Map<String, Object>>() {}
            );
        } catch (Exception e) {
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
