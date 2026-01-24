package com.moneybuddy.moneylog.main.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.challenge.dto.ChallengeDetailResponse;
import com.moneybuddy.moneylog.challenge.viewmodel.ChallengeViewModel;
import com.moneybuddy.moneylog.common.ApiService;
import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.common.ResultCallback;
import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.finance.activity.FinanceInfoActivity;
import com.moneybuddy.moneylog.finance.dto.response.QuizResponse;
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.repository.AnalyticsRepository;
import com.moneybuddy.moneylog.ledger.ui.CategoryColors;
import com.moneybuddy.moneylog.main.activity.MainMenuActivity;
import com.moneybuddy.moneylog.mobti.activity.MobtiActivity;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto;
import com.moneybuddy.moneylog.mobti.repository.MobtiRepository;
import com.moneybuddy.moneylog.mobti.util.MobtiMascot;
import com.moneybuddy.moneylog.notification.activity.NotificationActivity;
import com.moneybuddy.moneylog.notification.network.NotificationRepository;
import com.moneybuddy.moneylog.mypage.activity.MypageActivity;
import com.moneybuddy.moneylog.util.KoreanMoney;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@ExperimentalBadgeUtils
public class MainMenuHomeFragment extends Fragment {

    /* ================== 기존 원격 구조 ================== */
    private Button bellBtn, mypageBtn, toLedgerBtn, toChallengeBtn, toFinEdBtn, toMobtiBtn;
    private TextView quizQuestionText, mobtiNicknameText, mobtiEmojiText, textView7;
    private CircularProgressIndicator progressBar;

    private BadgeDrawable badge;
    private NotificationRepository notificationRepo;
    private MobtiRepository mobtiRepo;
    private ApiService apiService;
    private ChallengeViewModel challengeViewModel;

    /* ================== 홈 카드(네가 만든 기능) ================== */
    private TextView tvSpent, tvGoal;
    private LinearLayout goalBarTrack;

    private ImageView ivCat1, ivCat2, ivCat3;
    private TextView tvCat1, tvCat2, tvCat3;

    private AnalyticsRepository analyticsRepo;
    private int ratioReqSeq = 0;

    /* =================================================== */

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        challengeViewModel = new ViewModelProvider(requireActivity()).get(ChallengeViewModel.class);

        initViews(view);
        setupButtons();
        setupNotificationBadge();

        loadAndRenderMobti();
        loadAndRenderQuiz();
        loadRepresentativeChallenge();
        observeRepresentativeChallenge();

        initAnalytics();
        loadHomeCard(currentYearMonth());
    }

    /* ================== 초기화 ================== */

    private void initViews(View v) {
        bellBtn = v.findViewById(R.id.button2);
        mypageBtn = v.findViewById(R.id.button3);
        toLedgerBtn = v.findViewById(R.id.button4);
        toChallengeBtn = v.findViewById(R.id.button5);
        toFinEdBtn = v.findViewById(R.id.button6);
        toMobtiBtn = v.findViewById(R.id.button7);

        quizQuestionText = v.findViewById(R.id.textView4);
        mobtiNicknameText = v.findViewById(R.id.textView6);
        mobtiEmojiText = v.findViewById(R.id.textView5);
        textView7 = v.findViewById(R.id.textView7);
        progressBar = v.findViewById(R.id.progressBar);

        tvSpent = v.findViewById(R.id.textView8);
        tvGoal = v.findViewById(R.id.textView9);
        goalBarTrack = v.findViewById(R.id.goal_bar_track);

        ivCat1 = v.findViewById(R.id.imageView9);
        ivCat2 = v.findViewById(R.id.imageView10);
        ivCat3 = v.findViewById(R.id.imageView11);
        tvCat1 = v.findViewById(R.id.textView11);
        tvCat2 = v.findViewById(R.id.textView12);
        tvCat3 = v.findViewById(R.id.textView13);
    }

    /* ================== 홈 카드 ================== */

    private void initAnalytics() {
        String token = "";
        try {
            token = TokenManager.getInstance(requireContext()).getToken();
        } catch (Exception ignore) {}
        analyticsRepo = new AnalyticsRepository(requireContext(), token);
        applyDefaultHomeCard();
    }

    private String currentYearMonth() {
        Calendar cal = Calendar.getInstance();
        return String.format(Locale.KOREAN, "%04d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1);
    }

    private void applyDefaultHomeCard() {
        tvSpent.setText(KoreanMoney.format(0));
        tvGoal.setText(KoreanMoney.format(0));
        goalBarTrack.removeAllViews();
        setCategoryVisible(1, false, null, 0);
        setCategoryVisible(2, false, null, 0);
        setCategoryVisible(3, false, null, 0);
    }

    private void loadHomeCard(String ym) {
        final int mySeq = ++ratioReqSeq;

        analyticsRepo.getCategoryRatio(ym, new ResultCallback<CategoryRatioResponse>() {
            @Override
            public void onSuccess(CategoryRatioResponse dto) {
                if (!isAdded() || mySeq != ratioReqSeq) return;

                long spent = Math.max(0, dto.spent);
                long goal = dto.goalAmount == null ? 0 : Math.max(0, dto.goalAmount);

                tvSpent.setText(KoreanMoney.format(spent));
                tvGoal.setText(KoreanMoney.format(goal));

                renderGoalBar(dto);
                renderTopCategories(dto);
            }

            @Override
            public void onError(Throwable t) {
                if (!isAdded() || mySeq != ratioReqSeq) return;
                Toast.makeText(requireContext(), "홈 카드 로딩 실패", Toast.LENGTH_SHORT).show();
                applyDefaultHomeCard();
            }
        });
    }

    private void renderGoalBar(CategoryRatioResponse dto) {
        goalBarTrack.removeAllViews();

        long spent = Math.max(0, dto.spent);
        long goal = dto.goalAmount == null ? spent : Math.max(dto.goalAmount, 1);
        double baseline = goal > 0 ? goal : Math.max(spent, 1);

        if (dto.items == null) return;

        for (CategoryRatioResponse.Item it : dto.items) {
            if (it.expense <= 0) continue;

            View seg = new View(requireContext());
            seg.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT,
                    (float) (it.expense / baseline)
            ));
            seg.setBackgroundColor(CategoryColors.bg(requireContext(), it.category));
            goalBarTrack.addView(seg);
        }
    }

    private void renderTopCategories(CategoryRatioResponse dto) {
        if (dto.items == null) return;

        List<CategoryRatioResponse.Item> list = new ArrayList<>(dto.items);
        list.sort((a, b) -> Long.compare(b.expense, a.expense));

        setCategoryVisible(1, false, null, 0);
        setCategoryVisible(2, false, null, 0);
        setCategoryVisible(3, false, null, 0);

        for (int i = 0; i < Math.min(3, list.size()); i++) {
            CategoryRatioResponse.Item it = list.get(i);
            setCategoryVisible(
                    i + 1,
                    true,
                    it.category,
                    CategoryColors.bg(requireContext(), it.category)
            );
        }
    }

    private void setCategoryVisible(int idx, boolean visible, String label, int color) {
        ImageView iv = idx == 1 ? ivCat1 : idx == 2 ? ivCat2 : ivCat3;
        TextView tv = idx == 1 ? tvCat1 : idx == 2 ? tvCat2 : tvCat3;

        if (!visible) {
            iv.setVisibility(View.GONE);
            tv.setVisibility(View.GONE);
            return;
        }
        iv.setVisibility(View.VISIBLE);
        tv.setVisibility(View.VISIBLE);
        tv.setText(label);
        iv.setColorFilter(color);
    }

    /* ================== 나머지 원격 기능 ================== */

    private void setupButtons() {
        bellBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificationActivity.class)));
        mypageBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), MypageActivity.class)));
        toFinEdBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), FinanceInfoActivity.class)));
        toMobtiBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), MobtiActivity.class)));

        toLedgerBtn.setOnClickListener(v ->
                ((MainMenuActivity) requireActivity()).navigateToTab(R.id.menu_ledger));
        toChallengeBtn.setOnClickListener(v ->
                ((MainMenuActivity) requireActivity()).navigateToTab(R.id.menu_challenge));
    }

    private void setupNotificationBadge() {
        notificationRepo = new NotificationRepository(requireContext());
        badge = BadgeDrawable.create(requireContext());
        badge.setVisible(false);
        BadgeUtils.attachBadgeDrawable(badge, bellBtn);
    }

    private void loadAndRenderQuiz() {
        apiService = RetrofitClient.api(requireContext());
        apiService.getTodayQuiz().enqueue(new Callback<QuizResponse>() {
            @Override public void onResponse(@NonNull Call<QuizResponse> c, @NonNull Response<QuizResponse> r) {
                if (r.isSuccessful() && r.body() != null)
                    quizQuestionText.setText(r.body().getQuestion());
            }
            @Override public void onFailure(@NonNull Call<QuizResponse> c, @NonNull Throwable t) {}
        });
    }

    private void loadAndRenderMobti() {
        mobtiRepo = new MobtiRepository(requireContext());
        mobtiRepo.mySummary().enqueue(new Callback<MobtiBriefDto>() {
            @Override public void onResponse(@NonNull Call<MobtiBriefDto> c, @NonNull Response<MobtiBriefDto> r) {
                if (r.isSuccessful() && r.body() != null) {
                    mobtiNicknameText.setText(r.body().getNickname());
                    mobtiEmojiText.setText(MobtiMascot.emoji(r.body().getCode()));
                    mobtiEmojiText.setGravity(Gravity.CENTER);
                }
            }
            @Override public void onFailure(@NonNull Call<MobtiBriefDto> c, @NonNull Throwable t) {}
        });
    }

    private void loadRepresentativeChallenge() {
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        long id = prefs.getLong("representative_challenge_id", -1);
        if (id != -1) challengeViewModel.loadRepresentativeChallenge(id);
    }

    private void observeRepresentativeChallenge() {
        challengeViewModel.getRepresentativeChallenge().observe(getViewLifecycleOwner(), c -> {
            if (c == null) {
                textView7.setText("대표 챌린지를 설정해 보세요!");
                progressBar.setVisibility(View.GONE);
                return;
            }
            progressBar.setMax(Math.max(1, c.getGoalPeriodInDays()));
            progressBar.setProgress((int) c.getDaysSinceJoined());
            textView7.setText(c.getTitle() + "\n" +
                    c.getDaysSinceJoined() + " / " + c.getGoalPeriodInDays() + "일");
            progressBar.setVisibility(View.VISIBLE);
        });
    }
}
