package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserExp {

    @Id
    private Long userId;

    private Integer exp = 0;
    private Integer level = 1;
    private LocalDateTime lastLeveledUpAt;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}
