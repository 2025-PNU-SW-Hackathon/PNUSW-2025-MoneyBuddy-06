package com.moneybuddy.moneylog.activity.notifications;

public class Notice {
    private final String title;
    private final String message;
    private final long timestamp;
    private final boolean unread;

    public Notice(String title, String message, long timestamp, boolean unread) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.unread = unread;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isUnread() { return unread; }
}
