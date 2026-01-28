package com.moneybuddy.moneylog.controller;

import com.moneybuddy.moneylog.service.NotificationService;
import com.moneybuddy.moneylog.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public NotificationService.NotificationList list(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtils.currentUserId();
        return notificationService.list(userId, cursor, size);
    }

    @GetMapping("/unread-count")
    public long unreadCount() {
        Long userId = SecurityUtils.currentUserId();
        return notificationService.unreadCount(userId);
    }

    @PatchMapping("/{id}/read")
    public void markRead(@PathVariable Long id) {
        Long userId = SecurityUtils.currentUserId();
        notificationService.markRead(userId, id);
    }

    @PostMapping("/mark-all-read")
    public void markAllRead() {
        Long userId = SecurityUtils.currentUserId();
        notificationService.markAllRead(userId);
    }
}
