package com.moneybuddy.moneylog.challenge.adapter.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.challenge.dto.ChallengeCardResponse;

import java.util.function.BiConsumer;

public class TodoAdapter extends ListAdapter<ChallengeCardResponse, TodoAdapter.TodoViewHolder> {

    private final BiConsumer<Long, Boolean> onTodoChecked;

    public TodoAdapter(BiConsumer<Long, Boolean> onTodoChecked) {
        super(new DiffUtil.ItemCallback<ChallengeCardResponse>() {
            @Override
            public boolean areItemsTheSame(@NonNull ChallengeCardResponse oldItem, @NonNull ChallengeCardResponse newItem) {
                return oldItem.getChallengeId().equals(newItem.getChallengeId());
            }
            @Override
            public boolean areContentsTheSame(@NonNull ChallengeCardResponse oldItem, @NonNull ChallengeCardResponse newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }
        });
        this.onTodoChecked = onTodoChecked;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_todo_list_item, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;

        TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.cb_todo_item);
        }

        void bind(ChallengeCardResponse todo) {
            checkBox.setText(todo.getTitle());
            checkBox.setOnCheckedChangeListener(null);
             checkBox.setChecked(todo.getSuccess());
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                onTodoChecked.accept(todo.getChallengeId(), isChecked);
            });
        }
    }
}