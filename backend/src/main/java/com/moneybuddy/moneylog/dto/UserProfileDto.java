package com.moneybuddy.moneylog.dto;

public class UserProfileDto {
    private Long id;
    private String email;
    private String mobti;

    public UserProfileDto(Long id, String email, String mobti) {
        this.id = id;
        this.email = email;
        this.mobti = mobti;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getMobti() { return mobti; }
}
