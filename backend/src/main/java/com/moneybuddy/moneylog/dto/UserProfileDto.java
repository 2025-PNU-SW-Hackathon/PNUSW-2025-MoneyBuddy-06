package com.moneybuddy.moneylog.dto;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class UserProfileDto {
    private Long id;
    private String email;
    private String mobti;
    private LocalDateTime mobtiUpdatedAt;

    public UserProfileDto(Long id, String email, String mobti, LocalDateTime mobtiUpdatedAt) {
        this.id = id;
        this.email = email;
        this.mobti = mobti;
        this.mobtiUpdatedAt = mobtiUpdatedAt;
    }
}
