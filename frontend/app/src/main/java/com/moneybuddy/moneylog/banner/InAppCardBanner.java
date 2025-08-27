package com.moneybuddy.moneylog.banner;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import com.moneybuddy.moneylog.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * item_notification_card.xml 을 그대로 배너로 띄우는 유틸
 */
public final class InAppCardBanner {
    private static final String TAG = "ML_INAPP_CARD_BANNER";
    private static final long AUTO_DISMISS_MS = 3000;

    private InAppCardBanner() {}

    public static void show(Activity activity,
                            int iconRes,           // 아이콘 리소스 (0이면 숨김)
                            CharSequence title,    // tvTitle
                            CharSequence body,     // tvBody
                            CharSequence timeText, // tvTime (null이면 현재시각 표시)
                            Runnable onClick) {
        ViewGroup content = activity.findViewById(android.R.id.content);

        // 기존 배너 제거(중복 방지)
        View old = content.findViewWithTag(TAG);
        if (old != null) content.removeView(old);

        // 루트 컨테이너(FrameLayout) 준비
        BannerContainer container = new BannerContainer(activity);
        container.setTag(TAG);
        content.addView(container,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // 카드 레이아웃 inflate (그대로 재사용)
        View card = LayoutInflater.from(activity).inflate(R.layout.item_notification_card, container, false);
        container.addView(card);

        // 뷰 바인딩
        ImageView ivIcon = card.findViewById(R.id.ivIcon);
        TextView tvTitle  = card.findViewById(R.id.tvTitle);
        TextView tvBody   = card.findViewById(R.id.tvBody);
        TextView tvTime   = card.findViewById(R.id.tvTime);

        if (iconRes != 0) {
            ivIcon.setImageResource(iconRes);
            ivIcon.setVisibility(View.VISIBLE);
        } else {
            ivIcon.setVisibility(View.GONE);
        }
        tvTitle.setText(title);
        tvBody.setText(body);
        if (timeText == null) {
            String now = new SimpleDateFormat("yy.MM.dd HH:mm", Locale.getDefault()).format(new Date());
            tvTime.setText(now);
        } else {
            tvTime.setText(timeText);
        }

        // 클릭 동작
        card.setOnClickListener(v -> {
            dismiss(container);
            if (onClick != null) onClick.run();
        });

        // 스와이프(위로) 제거
        card.setOnTouchListener(new SwipeDismissTouchListener(card, () -> dismiss(container)));

        // 진입 애니메이션(위에서 내려오기)
        container.post(() -> {
            container.setTranslationY(-container.getHeight() - 60f);
            container.animate().translationY(0f).setDuration(220).start();
        });

        // 자동 닫힘
        new Handler(Looper.getMainLooper()).postDelayed(() -> dismiss(container), AUTO_DISMISS_MS);
    }

    private static void dismiss(View container) {
        ViewPropertyAnimator anim = container.animate().translationY(-container.getHeight() - 60f).setDuration(180);
        anim.withEndAction(() -> {
            ViewGroup parent = (ViewGroup) container.getParent();
            if (parent != null) parent.removeView(container);
        });
        anim.start();
    }

    /** 상단에 배치되는 간단 FrameLayout 컨테이너 */
    private static class BannerContainer extends androidx.appcompat.widget.LinearLayoutCompat {
        BannerContainer(Activity act) {
            super(act);
            setOrientation(VERTICAL);
            setPadding(16, getStatusBarHeight(), 16, 0); // 상태바 피해서 여백
            setGravity(Gravity.TOP);
        }
        private int getStatusBarHeight() {
            int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            return resId > 0 ? getResources().getDimensionPixelSize(resId) : 0;
        }
    }
}
