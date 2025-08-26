package com.moneybuddy.moneylog.main.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.notification.activity.NotificationActivity;
import com.moneybuddy.moneylog.notification.network.NotificationRepository;
import com.moneybuddy.moneylog.mypage.activity.MypageActivity;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainMenuHomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu_home, container, false);
    }

    private Button bellBtn;
    private BadgeDrawable badge;
    private NotificationRepository repo;

    @ExperimentalBadgeUtils
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bellBtn = view.findViewById(R.id.button2);
        bellBtn.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), NotificationActivity.class)));

        bellBtn = view.findViewById(R.id.button3);
        bellBtn.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MypageActivity.class)));

        // 버튼 기본 최소크기/패딩 제거 → 배지 붙는 위치 정확히
        bellBtn.setMinWidth(0);
        bellBtn.setMinHeight(0);
        bellBtn.setPadding(0, 0, 0, 0);

        // ✅ 실서버 레포
        repo = new NotificationRepository(requireContext());

        // 숫자 없는 점 배지 + 위치 미세조정
        badge = BadgeDrawable.create(requireContext());
        badge.setVisible(false);
        badge.setBadgeGravity(BadgeDrawable.TOP_END);
        badge.clearNumber();
        badge.setHorizontalOffset(dp(5));
        badge.setVerticalOffset(dp(5));
        BadgeUtils.attachBadgeDrawable(badge, bellBtn);
    }

    @ExperimentalBadgeUtils
    @Override
    public void onResume() {
        super.onResume();
        refreshUnreadBadge();
    }

    @ExperimentalBadgeUtils
    private void refreshUnreadBadge() {
        repo.getUnreadCount(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> res) {
                int count = (res.isSuccessful() && res.body() != null) ? res.body() : 0;

                //하나 이상이면 점만 표시
                badge.clearNumber();
                badge.setVisible(count > 0);

                BadgeUtils.attachBadgeDrawable(badge, bellBtn);
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                // 실패하면 안 보이게
                badge.setVisible(false);
                BadgeUtils.attachBadgeDrawable(badge, bellBtn);
            }
        });
    }

    private int dp(int dp) {
        float d = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * d);
    }
}
