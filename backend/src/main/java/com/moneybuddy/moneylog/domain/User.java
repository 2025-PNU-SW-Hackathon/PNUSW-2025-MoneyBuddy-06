package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Builder.Default
    private boolean isNotificationEnabled = true;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
