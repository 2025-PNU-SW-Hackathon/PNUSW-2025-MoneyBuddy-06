package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_device_token",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_token", columnNames = {"userId", "deviceToken"}),
        indexes = {
                @Index(name = "idx_udt_user", columnList = "userId"),
                @Index(name="idx_udt_token", columnList = "deviceToken", unique = true)
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserDeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String deviceToken;

    // 사용자가 이 기기에서 푸시 알림을 받겠다고 한 상태
    @Column(nullable=false)
    @Builder.Default
    private boolean enabled = true;

    // 비번 변경 후 재로그인 전까지 일반 푸시 차단
    @Column(nullable=false)
    @Builder.Default
    private boolean reauthRequired = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
