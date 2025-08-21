package com.moneybuddy.moneylog.network;

import android.os.Handler;
import android.os.Looper;
import com.moneybuddy.moneylog.model.Notice;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FakeNotificationRepository extends NotificationRepository {

    private final Handler main = new Handler(Looper.getMainLooper());
    private int unread = 3; // 초기 더미값

    @Override
    public void fetchList(Long cursor, int size, Callback<List<Notice>> cb) {
        main.postDelayed(() -> cb.onResponse(null, Response.success(fakeList())), 250);
    }

    @Override
    public void markRead(long id, Callback<Void> cb) {
        if (unread > 0) unread--;
        main.postDelayed(() -> cb.onResponse(null, Response.success(null)), 120);
    }

    @Override
    public void markAllRead(Callback<Void> cb) {
        unread = 0;
        main.postDelayed(() -> cb.onResponse(null, Response.success(null)), 180);
    }

    @Override
    public void unreadCount(Callback<Integer> cb) {
        main.postDelayed(() -> cb.onResponse(null, Response.success(unread)), 100);
    }

    private List<Notice> fakeList() {
        List<Notice> list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Notice n = new Notice();
            n.id = 1000 + i;
            n.type = (i % 2 == 0) ? "BUDGET_WARNING" : "QUIZ_TODAY";
            n.title = (i % 2 == 0) ? "이번 달 지출이 80%를 넘었어요" : "오늘의 금융 퀴즈";
            n.body = (i % 2 == 0) ? "통계에서 상세 확인" : "한 문제만 풀어도 포인트!";
            n.createdAt = "2025-08-21T14:0" + i + ":10";
            n.isRead = (i >= 3); // 더미
            n.action = (i % 2 == 0) ? "OPEN_SPENDING_STATS" : "OPEN_QUIZ_TODAY";
            n.params = new HashMap<>();
            if (i % 2 == 0) n.deeplink = "/stats/spending";
            list.add(n);
        }
        return list;
    }
}
