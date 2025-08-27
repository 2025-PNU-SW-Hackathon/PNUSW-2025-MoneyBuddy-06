package com.moneybuddy.moneylog.notification.activity;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.notification.adapter.NotificationAdapter;
import com.moneybuddy.moneylog.notification.model.FooterMarker;
import com.moneybuddy.moneylog.notification.model.ListResponse;
import com.moneybuddy.moneylog.notification.model.Notice;
import com.moneybuddy.moneylog.notification.network.NotificationRepository;
import com.moneybuddy.moneylog.util.DeepLinkResolver;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

public class NotificationActivity extends AppCompatActivity
        implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rv;
    private NotificationAdapter adapter;
    private NotificationRepository repo;

    private final List<Object> rows = new ArrayList<>();
    private Long nextCursor = null;

    private MenuItem markAllItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        rv = findViewById(R.id.rvNotifications);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this);
        rv.setAdapter(adapter);

        // 툴바 아래 첫 셀 여백 보정
        final int firstTop = getResources().getDimensionPixelSize(R.dimen.notif_list_first_item_top);
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, android.view.View view,
                                       RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) == 0) outRect.top += firstTop;
            }
        });

        repo = new NotificationRepository(this);
        loadFirstPage();
    }

    private void loadFirstPage() {
        repo.getNotifications(null, 20, new Callback<ListResponse>() { //  메서드명 수정
            @Override
            public void onResponse(Call<ListResponse> call, Response<ListResponse> res) {
                if (!res.isSuccessful() || res.body() == null || res.body().items == null) {
                    toast("알림 불러오기 실패");
                    return;
                }
                rows.clear();
                rows.addAll(res.body().items);
                rows.add(new FooterMarker());
                nextCursor = res.body().nextCursor;
                adapter.submit(rows);
            }
            @Override public void onFailure(Call<ListResponse> call, Throwable t) {
                toast("네트워크 오류: " + t.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 기존 메뉴 유지
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        markAllItem = menu.findItem(R.id.action_mark_all_read);

        // 메뉴 XML을 건드리지 않고 텍스트만 여기서 통일
        if (markAllItem != null) {
            markAllItem.setTitle("모두 읽음");

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_mark_all_read) {
            // 텍스트는 항상 "모두 읽음" 유지
            setMarkAllTitle("모두 읽음");
            item.setEnabled(false); // 중복 탭 방지

            repo.markAllRead(new Callback<Void>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) {
                        // 실패: 다시 활성화 + 기본 색으로 복구
                        item.setEnabled(true);
                        setMarkAllTitle("모두 읽음");
                        setMenuItemTextColor(markAllItem, getAttrColor(com.google.android.material.R.attr.colorOnSurface));
                        toast("모두 읽음 실패");
                        return;
                    }
                    // 화면상 모든 항목 읽음 처리
                    for (int i = 0; i < rows.size(); i++) {
                        Object o = rows.get(i);
                        if (o instanceof Notice) ((Notice) o).isRead = true;
                    }
                    adapter.submit(rows);

                    // 성공: 텍스트는 그대로, 옅은 색(Disabled 톤) 적용 + 비활성 유지
                    setMarkAllTitle("모두 읽음");
                    tintMarkAllAsDisabled();
                    item.setEnabled(false);
                    toast("모두 읽음 처리했어요");
                }
                @Override public void onFailure(Call<Void> call, Throwable t) {
                    item.setEnabled(true);
                    setMarkAllTitle("모두 읽음");
                    setMenuItemTextColor(markAllItem, getAttrColor(com.google.android.material.R.attr.colorOnSurface));
                    toast("모두 읽음 실패: " + t.getMessage());
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //  클릭 시 라우팅
    @Override
    public void onNotificationClick(Notice item) {
        // 1) 이동
        DeepLinkResolver.resolve(this, item.deeplink);

        // 2) 읽음 처리 (fire-and-forget)
        repo.markAsRead(item.id); //  토큰 인자 제거 (인터셉터가 자동 첨부)
        // 또는: repo.markRead(item.id, null);

        item.isRead = true;
        adapter.notifyDataSetChanged();
    }

    // 메뉴 아이템 텍스트 색 바꾸기
    private void setMenuItemTextColor(MenuItem item, int color) {
        if (item == null) return;
        CharSequence t = item.getTitle();
        if (t == null) t = "";
        SpannableString span = new SpannableString(t);
        span.setSpan(new ForegroundColorSpan(color), 0, span.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        item.setTitle(span);
    }

    // 테마 속성 색 가져오기 (없으면 회색)
    private int getAttrColor(int attr) {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(attr, tv, true)) {
            if (tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                return tv.data;
            } else if (tv.resourceId != 0) {
                return ContextCompat.getColor(this, tv.resourceId);
            }
        }
        return Color.GRAY;
    }

    // alpha 적용한 색 만들기 (예: 38% = 비활성 텍스트)
    private int withAlpha(int color, float alpha) {
        int a = Math.round(255 * alpha);
        int rgb = color & 0x00FFFFFF;
        return (a << 24) | rgb;
    }

    // 성공 후 “모두 읽음” 텍스트를 옅은 색으로
    private void tintMarkAllAsDisabled() {
        int onSurface = getAttrColor(com.google.android.material.R.attr.colorOnSurface);
        int disabled = withAlpha(onSurface, 0.38f); // 머터리얼 Disabled 톤
        setMenuItemTextColor(markAllItem, disabled);
    }

    private void toast(String m) { Toast.makeText(this, m, Toast.LENGTH_SHORT).show(); }


    private void setMarkAllTitle(String title) {
        if (markAllItem != null) markAllItem.setTitle(title);
    }

}

