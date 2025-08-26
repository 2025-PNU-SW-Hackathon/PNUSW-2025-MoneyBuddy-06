package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 추가
@AllArgsConstructor
@Builder
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    private LocalDateTime joinedAt;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false)
    private Boolean success = false;

    @Column(nullable = false)
    private Boolean rewarded = false;
}
