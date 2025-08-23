package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class UserExp {

    @Id
    private Long userId;

    private int experience = 0;

    private int level = 1;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private User user;

    public boolean addExperience(int amount, int expPerLevel) {
        this.experience += amount;
        boolean leveledUp = false;
        while (this.experience >= expPerLevel) {
            this.experience -= expPerLevel;
            this.level++;
            leveledUp = true;
        }
        return leveledUp;
    }
}

