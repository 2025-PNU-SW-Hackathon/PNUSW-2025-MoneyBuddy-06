package com.moneybuddy.moneylog.domain;

import jakarta.persistence.*;
import lombok.*;
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
  
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "mobti_type")
    private String mobti;

    @Column(name = "mobti_updated_at")
    private LocalDateTime mobtiUpdatedAt;
      
    @Column(nullable = false)
    private Integer score = 0;
  
    @Column(nullable = false)
    @Builder.Default
    private boolean isNotificationEnabled = true;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.score = 0;
    }
 
    public void increaseScore(int point) {
        this.score += point;
    }
}