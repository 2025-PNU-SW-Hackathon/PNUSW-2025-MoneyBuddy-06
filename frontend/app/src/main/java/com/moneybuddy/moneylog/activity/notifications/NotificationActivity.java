package com.moneybuddy.moneylog.activity.notifications;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.activity.notifications.Notice;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rv;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this);
        rv.setAdapter(adapter);

        // 더미 데이터
        List<Object> rows = new ArrayList<>();
        rows.add(new Notice("새 알림", "가계부 목표가 갱신되었습니다.", System.currentTimeMillis(), true));
        rows.add(new Notice("새 알림", "이번 주 소비 리포트가 도착했어요.", System.currentTimeMillis(), true));
        rows.add(new Notice("새 알림", "이번 달 지출이 예산의 70%를 넘었어요.", System.currentTimeMillis(), true));
        rows.add(new Notice("새 알림", "카테고리별 지출 분석을 확인해 보세요.", System.currentTimeMillis(), false));
        rows.add(new Notice("새 알림", "앱이 최신 버전으로 업데이트되었어요.", System.currentTimeMillis(), false));
        rows.add(new FooterMarker());

        adapter.submit(rows);
    }

    @Override
    public void onNotificationClick(Notice notice) {
        Toast.makeText(this, "클릭: " + notice.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: 상세 화면으로 이동 등 연결
    }
}
