package com.moneybuddy.moneylog.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationRequest {
    private String message;
    private LocalDateTime receivedAt;
    private Long userId;
}
