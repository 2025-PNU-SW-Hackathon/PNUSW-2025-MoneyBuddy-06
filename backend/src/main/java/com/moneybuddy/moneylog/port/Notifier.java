package com.moneybuddy.moneylog.port;

import com.moneybuddy.moneylog.model.*;
import java.util.Map;

public interface Notifier {
    void send(Long userId,
              NotificationType type,
              TargetType targetType,
              Long targetId,
              String title,
              String body,
              NotificationAction action,
              Map<String, Object> params,
              String deeplink);
}
