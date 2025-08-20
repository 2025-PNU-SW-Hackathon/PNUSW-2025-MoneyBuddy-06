package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class UserExp {

    @Id
    private Long userId;

    private int experience = 0;

    private int level = 1;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private User user;

    public void addExperience(int amount, int expPerLevel) {
        this.experience += amount;
        while (this.experience >= expPerLevel) {
            this.experience -= expPerLevel;
            this.level++;
        }
    }
}

