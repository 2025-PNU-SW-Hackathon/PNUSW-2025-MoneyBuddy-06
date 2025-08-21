package com.moneybuddy.moneylog.model;

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

    // ✅ 추가: 서버가 내려주는 경로 조립용 파라미터 (없을 수 있음)
    @Nullable
    public Map<String, Object> params;

    // 서버가 직접 내려주는 딥링크 (있으면 이걸 우선 사용)
    @Nullable
    public String deeplink;
}
