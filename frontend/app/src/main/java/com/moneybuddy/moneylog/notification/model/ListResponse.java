package com.moneybuddy.moneylog.notification.model;

import java.util.List;

/** GET /api/notifications 표준 응답 */
public class ListResponse {
    public List<Notice> items;
    public Long nextCursor; // 없으면 null
}
