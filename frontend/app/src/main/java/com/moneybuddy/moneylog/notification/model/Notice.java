package com.moneybuddy.moneylog.notification.model;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Notice {
    public long id;
    public String type;
    public String title;
    public String body;

    @SerializedName("createdAt")
    public String createdAt;

    @SerializedName("isRead")
    public boolean isRead;

    // 이동 정보
    public String action;

    @Nullable
    public Map<String, Object> params;

    @Nullable
    public String deeplink;
}
