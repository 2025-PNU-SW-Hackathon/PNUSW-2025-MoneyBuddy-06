package com.moneybuddy.moneylog.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.adapter.NotificationAdapter;
import com.moneybuddy.moneylog.model.FooterMarker;
import com.moneybuddy.moneylog.model.Notice;
import com.moneybuddy.moneylog.network.NotificationRepository;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rv;
    private NotificationAdapter adapter;
    private NotificationRepository repo;

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

        // 상단 여백(툴바 밑에서 시작)
        final int firstTop = getResources().getDimensionPixelSize(R.dimen.notif_list_first_item_top);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(android.graphics.Rect outRect, android.view.View view,
                                       RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.top += firstTop;
                }
            }
        });

        // ✅ 실서버 레포만 사용
        repo = new NotificationRepository(this);

        loadFirstPage();
    }

    private void loadFirstPage() {
        repo.fetchList(null, 20, new Callback<List<Notice>>() {
            @Override
            public void onResponse(Call<List<Notice>> call, Response<List<Notice>> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(NotificationActivity.this, "알림 불러오기 실패", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Object> rows = new ArrayList<>(res.body());
                rows.add(new FooterMarker());
                adapter.submit(rows);
            }

            @Override
            public void onFailure(Call<List<Notice>> call, Throwable t) {
                Toast.makeText(NotificationActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNotificationClick(Notice notice) {
        repo.markRead(notice.id, new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }
}
