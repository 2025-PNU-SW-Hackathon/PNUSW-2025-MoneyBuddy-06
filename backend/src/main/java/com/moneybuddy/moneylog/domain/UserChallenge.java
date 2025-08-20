package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    private LocalDateTime joinedAt;

    @Column(nullable = false)
    private Boolean completed = false;

    @Column(nullable = false)
    private Boolean rewarded = false;


}
