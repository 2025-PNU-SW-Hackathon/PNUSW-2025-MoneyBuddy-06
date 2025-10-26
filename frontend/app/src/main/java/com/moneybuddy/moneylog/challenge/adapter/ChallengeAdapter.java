package com.moneybuddy.moneylog.challenge.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.challenge.activity.ChallengeDetailActivity;
import com.moneybuddy.moneylog.challenge.dto.ChallengeCardResponse;
import com.moneybuddy.moneylog.challenge.dto.RecommendedChallengeResponse;
import com.moneybuddy.moneylog.challenge.model.ChallengeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import lombok.Setter;

public class ChallengeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_TODO_HEADER = 0;
    private static final int VIEW_TYPE_ONGOING_CHALLENGE = 1;
    private static final int VIEW_TYPE_DEFAULT_CHALLENGE = 2;

    private final Context context;
    private final BiConsumer<Long, Boolean> onTodoCheckedListener;
    private final OnRepresentativeChallengeClickListener repChallengeClickListener;

    // 대표 챌린지 challengeId를 저장할 변수
    @Setter
    private Long representativeChallengeId = -1L;

    private List<ChallengeCardResponse> items = new ArrayList<>();
    private List<ChallengeCardResponse> todoList = new ArrayList<>();
    private boolean showHeader = false;

    // 현재 필터, 디폴트 값 ongoing
    private ChallengeFilter currentFilter = ChallengeFilter.ONGOING;


    public ChallengeAdapter(Context context, BiConsumer<Long, Boolean> listener, OnRepresentativeChallengeClickListener repClickListener) {
        this.context = context;
        this.onTodoCheckedListener = listener;
        this.repChallengeClickListener = repClickListener;
    }

    public void setCurrentFilter(ChallengeFilter filter) {
        this.currentFilter = filter;
    }

    public interface OnRepresentativeChallengeClickListener {
        void onRepresentativeChallengeClick(Long challengeId);
    }


    public void submitList(List<ChallengeCardResponse> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setTodoList(List<ChallengeCardResponse> newTodoList) {
        this.todoList = newTodoList;
        if (showHeader) notifyItemChanged(0);
    }

    public void setShowHeader(boolean show) {
        this.showHeader = show;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (showHeader && position == 0) {
            return VIEW_TYPE_TODO_HEADER;
        }

        if (currentFilter == ChallengeFilter.ONGOING) {
            return VIEW_TYPE_ONGOING_CHALLENGE;
        } else {
            return VIEW_TYPE_DEFAULT_CHALLENGE;
        }
    }


    @Override
    public int getItemCount() {
        return items.size() + (showHeader ? 1 : 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_TODO_HEADER:
                View headerView = LayoutInflater.from(context).inflate(R.layout.item_todo_header, parent, false);
                return new TodoHeaderViewHolder(headerView, onTodoCheckedListener);

            case VIEW_TYPE_ONGOING_CHALLENGE:
                View ongoingView = LayoutInflater.from(context).inflate(R.layout.item_ongoing_challenge, parent, false);
                return new OngoingChallengeViewHolder(ongoingView);

            case VIEW_TYPE_DEFAULT_CHALLENGE:
            default:
                View defaultView = LayoutInflater.from(context).inflate(R.layout.item_challenge, parent, false);
                return new ChallengeViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TodoHeaderViewHolder) {
            ((TodoHeaderViewHolder) holder).bind(todoList);
        } else {
            Object item = items.get(showHeader ? position - 1 : position);
            if (holder instanceof OngoingChallengeViewHolder) {
                ((OngoingChallengeViewHolder) holder).bind((ChallengeCardResponse) item);
            } else if (holder instanceof ChallengeViewHolder) {
                ((ChallengeViewHolder) holder).bind(item);
            }
        }
    }

    class OngoingChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPeriod, tvValue;
        CircularProgressIndicator progressBar;
        ImageButton btnRepChallenge;
        ImageView ivCategory;
        View itemLayout;

        OngoingChallengeViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_challenge_title);
            tvPeriod = v.findViewById(R.id.tv_challenge_period);
            tvValue = v.findViewById(R.id.tv_challenge_value);
            progressBar = v.findViewById(R.id.progressBar);
            btnRepChallenge = v.findViewById(R.id.btn_rep_challenge);
            ivCategory = v.findViewById(R.id.iv_category);
            itemLayout = v.findViewById(R.id.challenge_item_layout);
        }


        void bind(ChallengeCardResponse c) {
            tvTitle.setText(c.getTitle());

            tvPeriod.setText("목표 기간: " + c.getGoalPeriod());

            if ("금액".equals(c.getGoalType())) {
                tvValue.setText("목표 금액: " + c.getGoalValue() + "원");
            } else {
                tvValue.setText("목표 횟수: " + c.getGoalValue() + "회");
            }

            progressBar.setMax(c.getGoalPeriodInDays());
            progressBar.setProgress((int) c.getDaysSinceJoined());

            if (c.getCategory() != null) {
                switch (c.getCategory()) {
                    case "식비":
                        ivCategory.setImageResource(R.drawable.category_food);
                        break;
                    case "교통":
                        ivCategory.setImageResource(R.drawable.category_transport);
                        break;
                    case "문화여가":
                        ivCategory.setImageResource(R.drawable.category_culture);
                        break;
                    case "의료건강":
                        ivCategory.setImageResource(R.drawable.category_health);
                        break;
                    case "의류미용":
                        ivCategory.setImageResource(R.drawable.category_beauty);
                        break;
                    case "카페베이커리":
                        ivCategory.setImageResource(R.drawable.category_cafe);
                        break;
                    case "저축":
                        ivCategory.setImageResource(R.drawable.category_saving);
                        break;
                    case "습관":
                        ivCategory.setImageResource(R.drawable.category_habit);
                        break;
                    default:
                        ivCategory.setImageResource(R.drawable.category_others);
                        break;
                }
            } else {
                ivCategory.setImageResource(R.drawable.category_others);
            }

            if (itemLayout != null && c.isMine()) {
                itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.my_challenge_color));
                progressBar.setTrackColor(ContextCompat.getColor(context, R.color.my_challenge_progressbar_track));
                progressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.my_challenge_progressbar_indicator));
            } else if (itemLayout != null){
                itemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.other_challenge_color));
                progressBar.setTrackColor(ContextCompat.getColor(context, R.color.other_challenge_progressbar_track));
                progressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.other_challenge_progressbar_indicator));
            }

            itemView.setOnClickListener(v -> {
                Intent i = new Intent(context, ChallengeDetailActivity.class);
                i.putExtra("challenge", c);
                context.startActivity(i);
            });

            btnRepChallenge.setOnClickListener(v -> {
                if (repChallengeClickListener != null) {
                    repChallengeClickListener.onRepresentativeChallengeClick(c.getChallengeId());
                }
            });

            if (c.getChallengeId().equals(representativeChallengeId)) {
                btnRepChallenge.setForegroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.black)));
            } else {
                btnRepChallenge.setForegroundTintList(null);
            }
        }
    }

    class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPeriod, tvValue;
        ImageView ivCategory;
        LinearLayout layout;

        ChallengeViewHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tv_challenge_title);
            tvPeriod = v.findViewById(R.id.tv_challenge_period);
            tvValue = v.findViewById(R.id.tv_challenge_value);
            ivCategory = v.findViewById(R.id.iv_category);
            layout = v.findViewById(R.id.challenge_item_layout);
        }

        void bind(Object item) {
            String title, period, category, goalType;
            Integer goalValue;

            if (item instanceof ChallengeCardResponse) {
                ChallengeCardResponse c = (ChallengeCardResponse) item;
                title = c.getTitle();
                period = c.getGoalPeriod();
                category = c.getCategory();
                goalType = c.getGoalType();
                goalValue = c.getGoalValue();

                layout.setBackgroundColor(ContextCompat.getColor(context, Boolean.TRUE.equals(c.isMine()) ? R.color.my_challenge_color : R.color.other_challenge_color));
                itemView.setOnClickListener(v -> {
                    Intent i = new Intent(context, ChallengeDetailActivity.class);
                    i.putExtra("challenge", c);
                    context.startActivity(i);
                });

            } else if (item instanceof RecommendedChallengeResponse) {
                RecommendedChallengeResponse c = (RecommendedChallengeResponse) item;
                title = c.getTitle();
                period = c.getGoalPeriod();
                category = c.getCategory();
                goalType = c.getGoalType();
                goalValue = c.getGoalValue();

                layout.setBackgroundColor(ContextCompat.getColor(context, R.color.other_challenge_color));
                itemView.setOnClickListener(v -> {
                    ChallengeCardResponse challengeToPass = new ChallengeCardResponse(c);
                    Intent i = new Intent(context, ChallengeDetailActivity.class);
                    i.putExtra("challenge", challengeToPass);
                    context.startActivity(i);
                });

            } else {
                return;
            }

            tvTitle.setText(title);
            tvPeriod.setText("기간: " + period);

            if (goalValue == null || goalValue == 0) {
                tvValue.setText("");
            } else if ("금액".equals(goalType)) {
                tvValue.setText("금액: " + goalValue + "원");
            } else {
                tvValue.setText("횟수: " + goalValue + "회");
            }

            if (category != null) {
                switch (category) {
                    case "식비":
                        ivCategory.setImageResource(R.drawable.category_food);
                        break;
                    case "교통":
                        ivCategory.setImageResource(R.drawable.category_transport);
                        break;
                    case "문화여가":
                        ivCategory.setImageResource(R.drawable.category_culture);
                        break;
                    case "의료건강":
                        ivCategory.setImageResource(R.drawable.category_health);
                        break;
                    case "의류미용":
                        ivCategory.setImageResource(R.drawable.category_beauty);
                        break;
                    case "카페베이커리":
                        ivCategory.setImageResource(R.drawable.category_cafe);
                        break;
                    case "저축":
                        ivCategory.setImageResource(R.drawable.category_saving);
                        break;
                    case "습관":
                        ivCategory.setImageResource(R.drawable.category_habit);
                        break;
                    default:
                        ivCategory.setImageResource(R.drawable.category_others);
                        break;
                }
            } else {
                ivCategory.setImageResource(R.drawable.category_others);
            }
        }
    }

    class TodoHeaderViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rvTodoList;
        TextView tvEmptyTodo;
        TodoAdapter todoAdapter;

        TodoHeaderViewHolder(@NonNull View v, BiConsumer<Long, Boolean> onTodoChecked) {
            super(v);
            rvTodoList = v.findViewById(R.id.rv_todo_list);
            tvEmptyTodo = v.findViewById(R.id.tv_empty_todo);
            todoAdapter = new TodoAdapter(onTodoChecked);
            rvTodoList.setLayoutManager(new LinearLayoutManager(context));
            rvTodoList.setAdapter(todoAdapter);
        }

        void bind(List<ChallengeCardResponse> todos) {
            boolean isEmpty = todos == null || todos.isEmpty();
            rvTodoList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            tvEmptyTodo.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            if (!isEmpty) todoAdapter.submitList(todos);
        }
    }
}