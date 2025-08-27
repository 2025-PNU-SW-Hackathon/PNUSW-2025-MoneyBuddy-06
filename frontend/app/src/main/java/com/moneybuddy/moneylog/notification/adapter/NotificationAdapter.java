package com.moneybuddy.moneylog.notification.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.notification.model.Notice;
import com.moneybuddy.moneylog.notification.viewholder.FooterViewHolder;
import com.moneybuddy.moneylog.notification.viewholder.NoticeViewHolder;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notice item);
    }

    private final List<Object> data = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Object> items) {
        data.clear();
        data.addAll(items);
        notifyDataSetChanged();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VT_NOTICE, VT_FOOTER})
    @interface VT {}
    private static final int VT_NOTICE = 0;
    private static final int VT_FOOTER = 1;

    @Override public int getItemViewType(int position) {
        Object o = data.get(position);
        return (o instanceof Notice) ? VT_NOTICE : VT_FOOTER;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == VT_NOTICE) {
            View v = inf.inflate(R.layout.item_notification_card, parent, false);
            //  수정: 뷰홀더를 View 하나로만 생성
            return new NoticeViewHolder(v);
        } else {
            View v = inf.inflate(R.layout.item_footer_recent, parent, false);
            return new FooterViewHolder(v);
        }
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object o = data.get(position);
        if (holder instanceof NoticeViewHolder && o instanceof Notice) {
            Notice n = (Notice) o;
            ((NoticeViewHolder) holder).bind(n);
            //  수정: 클릭 리스너는 여기서 바인딩
            holder.itemView.setOnClickListener(v -> listener.onNotificationClick(n));
        }
        // Footer는 바인딩 로직 없음(정적 문구)
    }

    @Override public int getItemCount() { return data.size(); }
}
