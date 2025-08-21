package com.moneybuddy.moneylog.viewholder;

import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.model.Notice;

public class NoticeViewHolder extends RecyclerView.ViewHolder {
    private final ImageView icon;
    private final TextView title, body, time;

    public NoticeViewHolder(View itemView) {
        super(itemView);
        icon  = itemView.findViewById(R.id.ivIcon);
        title = itemView.findViewById(R.id.tvTitle);
        body  = itemView.findViewById(R.id.tvBody);
        time  = itemView.findViewById(R.id.tvTime);
    }

    public void bind(Notice n) {
        // 텍스트 바인딩
        title.setText(n.title);
        body.setText(n.body);
        time.setText(n.createdAt);

        // 읽음/안읽음 시각 구분 (점뷰 없이 처리)
        final boolean read = n.isRead;
        title.setAlpha(read ? 0.6f : 1f);
        body.setAlpha(read ? 0.6f : 1f);
        title.setTypeface(Typeface.DEFAULT, read ? Typeface.NORMAL : Typeface.BOLD);

        // 아이콘 바인딩 (아이콘 리소스 이름 그대로 사용)
        icon.setImageResource(iconOf(n));
    }

    private int iconOf(Notice n) {
        // action 우선, 없으면 type 기준
        String a = n.action == null ? "" : n.action;
        switch (a) {
            case "OPEN_SPENDING_STATS":   return R.drawable.icon_ledger;
            case "OPEN_LEDGER_NEW":       return R.drawable.icon_ledger;
            case "OPEN_QUIZ_TODAY":       return R.drawable.icon_quiz;
            case "OPEN_FINANCE_ARTICLE":  return R.drawable.icon_article;
            case "OPEN_CHALLENGE_DETAIL": return R.drawable.icon_challenge;
            case "OPEN_PROFILE_LEVEL":    return R.drawable.icon_mypage;
        }

        String t = n.type == null ? "" : n.type;
        switch (t) {
            case "BUDGET_WARNING":   return R.drawable.icon_ledger;
            case "LEDGER_REMINDER":  return R.drawable.icon_ledger;
            case "QUIZ_TODAY":       return R.drawable.icon_quiz;
            case "FINANCE_INFO":     return R.drawable.icon_article;
            case "CHALLENGE_SUCCESS":
            case "CHALLENGE_FAIL":   return R.drawable.icon_challenge;
            case "LEVEL_UP":         return R.drawable.icon_mypage;
            default:                 return R.drawable.icon_article; // 기본 아이콘
        }
    }
}
