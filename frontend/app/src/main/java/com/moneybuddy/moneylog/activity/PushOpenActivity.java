package com.moneybuddy.moneylog.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.moneybuddy.moneylog.activity.NotificationActivity;
import com.moneybuddy.moneylog.network.PushTrackingRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * 푸시 클릭 시 가장 먼저 열리는 Activity.
 * - 백엔드에 "opened" 기록
 * - 딥링크(있으면)로 라우팅, 없으면 알림센터로 이동
 * - 즉시 finish()
 */
public class PushOpenActivity extends AppCompatActivity {

    private PushTrackingRepository repo;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repo = new PushTrackingRepository(getApplicationContext());

        Map<String, String> data = new HashMap<>();
        if (getIntent() != null && getIntent().getExtras() != null) {
            for (String k : getIntent().getExtras().keySet()) {
                Object v = getIntent().getExtras().get(k);
                if (v != null) data.put(k, v.toString());
            }
        }

        long nid = 0;
        try { nid = Long.parseLong(data.getOrDefault("notificationId", "0")); } catch (Exception ignored) {}

        // 1) 열림 기록 (fire-and-forget)
        repo.trackOpened(nid, data);

        // 2) 라우팅
        String deeplink = data.get("deeplink"); // e.g. "/finance/6801"
        if (deeplink != null && !deeplink.isEmpty()) {
            try {
                // 앱에서 이 Uri를 해석하도록 매니페스트 딥링크가 잡혀 있으면 바로 이동
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(deeplink.startsWith("http") ? deeplink : "moneylog://" + deeplink));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            } catch (Exception e) {
                // 폴백: 알림센터
                startActivity(new Intent(this, NotificationActivity.class));
            }
        } else {
            startActivity(new Intent(this, NotificationActivity.class));
        }

        finish();
    }
}
