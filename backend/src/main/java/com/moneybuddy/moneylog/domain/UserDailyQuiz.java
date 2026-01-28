package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_daily_quiz",
        indexes = {
                @Index(name = "idx_udq_user", columnList = "user_id"),
                @Index(name = "idx_udq_completed", columnList = "user_id, completed")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDailyQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 유저인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 어떤 퀴즈인지
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // 문제를 받은 시각
    @Column(nullable = false)
    private LocalDateTime assignedAt;

    // 풀었는지 여부
    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false;

    // 사용자가 고른 답 (기본 false)
    @Column(nullable = false)
    @Builder.Default
    private boolean selectedAnswer = false;

    // 정답 여부 (기본 false)
    @Column(nullable = false)
    @Builder.Default
    private boolean correct = false;


    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
