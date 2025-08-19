package com.moneybuddy.moneylog.entity;

import com.moneybuddy.moneylog.model.NotificationAction;
import com.moneybuddy.moneylog.model.NotificationType;
import com.moneybuddy.moneylog.model.TargetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;   // 수신자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private TargetType targetType;

    private Long targetId;

    @Column(length = 200)
    private String title;

    @Column(length = 1000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private NotificationAction action;   // 프론트 라우팅용

    @Column(columnDefinition = "TEXT")
    private String paramsJson;    // 라우팅 파라미터

    @Column(length = 255)
    private String deeplink;    // 서버에서 조립한 내부 경로(/challenges/45 등)

    @Column(nullable = false)
    private boolean isRead = false;

    private LocalDateTime readAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (expiresAt == null) expiresAt = createdAt.plusDays(30);
    }
}
