package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "revoked_access_tokens", indexes = {
        @Index(name = "idx_revoked_jti", columnList = "jti", unique = true),
        @Index(name = "idx_revoked_expires_at", columnList = "expiresAt")
})
public class RevokedAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // JWT ID (jti)
    @Column(nullable = false, unique = true, length = 64)
    private String jti;

    // 토큰 만료 시각
    @Column(nullable = false)
    private LocalDateTime expiresAt;
}