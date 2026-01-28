package com.moneybuddy.moneylog.adapter;

import com.moneybuddy.moneylog.model.NotificationAction;
import com.moneybuddy.moneylog.model.NotificationType;
import com.moneybuddy.moneylog.model.TargetType;
import com.moneybuddy.moneylog.port.Notifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Primary
public class NoOpNotifier implements Notifier {
    @Override public void send(Long userId, NotificationType type, TargetType targetType,
                               Long targetId, String title, String body, NotificationAction action,
                               Map<String, Object> params, String deeplink) { /* no-op */ }
}
