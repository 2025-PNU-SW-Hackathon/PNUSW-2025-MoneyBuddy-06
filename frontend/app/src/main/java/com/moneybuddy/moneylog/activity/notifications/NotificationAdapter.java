package com.moneybuddy.moneylog.activity.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notice notice);
    }

    private static final int VT_ITEM   = 0;
    private static final int VT_AD     = 1;
    private static final int VT_FOOTER = 2;

    private final List<Object> items = new ArrayList<>();
    private final OnNotificationClickListener listener;

    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<Object> rows) {
        items.clear();
        if (rows != null) items.addAll(rows);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = items.get(position);
        if (o instanceof Notice) return VT_ITEM;
        return VT_FOOTER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == VT_ITEM) {
            View v = inf.inflate(R.layout.item_notification_card, parent, false);
            return new ItemVH(v, listener);
        }
        else {
            View v = inf.inflate(R.layout.item_footer_recent, parent, false);
            return new FooterVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VT_ITEM) {
            ((ItemVH) holder).bind((Notice) items.get(position));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    /* ---------------- ViewHolders ---------------- */
    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvTitle;
        Notice current;

        ItemVH(@NonNull View itemView, OnNotificationClickListener listener) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            itemView.setOnClickListener(v -> {
                if (listener != null && current != null) listener.onNotificationClick(current);
            });
        }

        void bind(Notice n) {
            current = n;
            tvTitle.setText(n.getTitle());
        }
    }

    static class AdVH extends RecyclerView.ViewHolder {
        AdVH(@NonNull View itemView) { super(itemView); }
    }

    static class FooterVH extends RecyclerView.ViewHolder {
        FooterVH(@NonNull View itemView) { super(itemView); }
    }
}
