package com.moneybuddy.moneylog.main.fragment;

import android.content.Intent;
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
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.challenge.dto.ChallengeDetailResponse;
import com.moneybuddy.moneylog.common.ApiService; // Import ApiService
import com.moneybuddy.moneylog.common.RetrofitClient; // Import RetrofitClient
import com.moneybuddy.moneylog.finance.activity.FinanceInfoActivity;
import com.moneybuddy.moneylog.finance.dto.response.QuizResponse; // Import QuizResponse
import com.moneybuddy.moneylog.ledger.dto.response.CategoryRatioResponse;
import com.moneybuddy.moneylog.ledger.loader.CategoryRatioLoader;
import com.moneybuddy.moneylog.mobti.activity.MobtiActivity;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto;
import com.moneybuddy.moneylog.mobti.repository.MobtiRepository;
import com.moneybuddy.moneylog.mobti.util.MobtiMascot;
import com.moneybuddy.moneylog.notification.activity.NotificationActivity;
import com.moneybuddy.moneylog.notification.network.NotificationRepository;
import com.moneybuddy.moneylog.mypage.activity.MypageActivity;
import com.moneybuddy.moneylog.challenge.viewmodel.ChallengeViewModel;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@ExperimentalBadgeUtils
public class MainMenuHomeFragment extends Fragment {

    private Button bellBtn, mypageBtn, toLedgerBtn, toChallengeBtn, toFinEdBtn, toMobtiBtn;
    private LinearLayout goalBarTrack;
    private TextView monthTitle, spentAmountText, goalAmountText;
    private TextView legendText1, legendText2, legendText3;
    private ImageView legendIcon1, legendIcon2, legendIcon3;
    private View legendLayout;
    private TextView mobtiNicknameText;
    private TextView mobtiEmojiText;
    private TextView quizQuestionText;
    private TextView textView7;
    private CircularProgressIndicator progressBar;
    private ChallengeViewModel challengeViewModel;

    private BadgeDrawable badge;
    private NotificationRepository notificationRepo;
    private MobtiRepository mobtiRepo;
    private ApiService apiService;

    private final int[] CATEGORY_COLORS = new int[]{0xFF376829, 0xFF50953C, 0xFFA0CE56, 0xFF888888};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        challengeViewModel = new ViewModelProvider(requireActivity()).get(ChallengeViewModel.class);

        initializeViews(view);
        setupNotificationBadge();
        setupButtonClickListeners(view);

        loadAndRenderAnalytics();
        loadAndRenderMobti();
        loadAndRenderQuiz();

        loadRepresentativeChallenge();
        observeRepresentativeChallenge();
    }


    private void initializeViews(View view) {
        bellBtn = view.findViewById(R.id.button2);
        mypageBtn = view.findViewById(R.id.button3);
        toLedgerBtn = view.findViewById(R.id.button4);
        toChallengeBtn = view.findViewById(R.id.button5);
        toFinEdBtn = view.findViewById(R.id.button6);
        toMobtiBtn = view.findViewById(R.id.button7);
        goalBarTrack = view.findViewById(R.id.goal_bar_track);
        monthTitle = view.findViewById(R.id.textView1);
        spentAmountText = view.findViewById(R.id.textView8);
        goalAmountText = view.findViewById(R.id.textView9);
        legendLayout = view.findViewById(R.id.layoutLabel);
        legendText1 = view.findViewById(R.id.textView11);
        legendText2 = view.findViewById(R.id.textView12);
        legendText3 = view.findViewById(R.id.textView13);
        legendIcon1 = view.findViewById(R.id.imageView9);
        legendIcon2 = view.findViewById(R.id.imageView10);
        legendIcon3 = view.findViewById(R.id.imageView11);
        mobtiNicknameText = view.findViewById(R.id.textView6);
        mobtiEmojiText = view.findViewById(R.id.textView5);

        quizQuestionText = view.findViewById(R.id.textView4);
        textView7 = view.findViewById(R.id.textView7);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void loadRepresentativeChallenge() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        long challengeId = prefs.getLong("representative_challenge_id", -1L);

        if (challengeId != -1L) {
            challengeViewModel.loadRepresentativeChallenge(challengeId);
        } else {
            textView7.setText("대표 챌린지가 설정되지 않았습니다.");
            Log.d("HomeFragment", "대표 챌린지가 설정되지 않았습니다.");
        }
    }
    private void observeRepresentativeChallenge() {
        challengeViewModel.getRepresentativeChallenge().observe(getViewLifecycleOwner(), challenge -> {
            if (challenge != null) {
                updateChallengeUI(challenge);
            } else {
                // ▼▼▼ ViewModel에서 null을 받았을 때 UI를 초기화하도록 추가 ▼▼▼
                setNoRepresentativeChallengeUI();
            }
        });

        challengeViewModel.getRepresentativeChallengeCleared().observe(getViewLifecycleOwner(), cleared -> {
            if (cleared != null && cleared) {
                setNoRepresentativeChallengeUI();
            }
        });
    }

    private void setNoRepresentativeChallengeUI() {
        if (textView7 == null || progressBar == null) return;

        progressBar.setVisibility(View.GONE);
        textView7.setText("대표 챌린지를 설정해 보세요!");
        Log.d("HomeFragment", "대표 챌린지가 설정되지 않았거나 해제되었습니다.");

    }

    private void loadAndRenderQuiz() {
        if (apiService == null) {
            apiService = RetrofitClient.api(requireContext());
        }

        apiService.getTodayQuiz().enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuizResponse> call, @NonNull Response<QuizResponse> response) {
                if (response.isSuccessful() && response.body() != null && getContext() != null) {
                    QuizResponse quiz = response.body();
                    quizQuestionText.setText(quiz.getQuestion());
                } else {
                    Log.e("MainMenuHome", "Quiz Response Error: " + response.code());
                    quizQuestionText.setText("퀴즈를 불러오는 데 실패했어요.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuizResponse> call, @NonNull Throwable t) {
                Log.e("MainMenuHome", "Quiz API Call Failure", t);
                quizQuestionText.setText("퀴즈를 불러오는 데 실패했어요.");
            }
        });
    }


    private void loadAndRenderMobti() {
        if (mobtiRepo == null) {
            mobtiRepo = new MobtiRepository(requireContext());
        }
        mobtiRepo.mySummary().enqueue(new Callback<MobtiBriefDto>() {
            @Override
            public void onResponse(@NonNull Call<MobtiBriefDto> call, @NonNull Response<MobtiBriefDto> response) {
                if (response.isSuccessful() && response.body() != null && getContext() != null) {
                    MobtiBriefDto dto = response.body();
                    mobtiNicknameText.setText(dto.getNickname());
                    String emoji = MobtiMascot.emoji(dto.getCode());
                    mobtiEmojiText.setGravity(Gravity.CENTER);
                    mobtiEmojiText.setText(emoji);
                }
            }
            @Override
            public void onFailure(@NonNull Call<MobtiBriefDto> call, @NonNull Throwable t) {
                Log.e("MainMenuHome", "Failed to load MoBTI summary", t);
            }
        });
    }

    private void loadAndRenderAnalytics() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM", Locale.getDefault());
        String currentYm = sdf.format(calendar.getTime());

        CategoryRatioLoader.load(requireContext(), currentYm, new CategoryRatioLoader.OnLoaded() {
            @Override
            public void onLoaded(CategoryRatioResponse res) {
                if (res != null && res.items != null && getContext() != null) {
                    updateAnalyticsUI(res);
                } else if (legendLayout != null) {
                    legendLayout.setVisibility(View.GONE);
                }
            }
            @Override
            public void onError(Throwable t) {
                Log.e("MainMenuHome", "Failed to load category ratio", t);
                if (legendLayout != null) {
                    legendLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateChallengeUI(ChallengeDetailResponse challenge) {
        if (textView7 == null || progressBar == null) return;

        progressBar.setVisibility(View.VISIBLE);

        String title = challenge.getTitle();
        long daysSinceJoined = challenge.getDaysSinceJoined();
        int goalPeriodInt = challenge.getGoalPeriodInDays();

        // 목표 기간이 0일 경우 progressBar가 오동작하는 것을 방지
        progressBar.setMax(goalPeriodInt > 0 ? goalPeriodInt : 1);
        progressBar.setProgress((int) daysSinceJoined);

        String progressText = title + "\n" + daysSinceJoined + "일 / " + goalPeriodInt + "일";
        textView7.setText(progressText);
    }

    private void updateAnalyticsUI(CategoryRatioResponse data) {
        try {
            String monthStr = data.yearMonth.substring(4, 6);
            monthTitle.setText(String.format(Locale.getDefault(), "%d월 소비", Integer.parseInt(monthStr)));
        } catch (Exception e) {
            monthTitle.setText("이번 달 소비");
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        spentAmountText.setText(String.format("%s원", formatter.format(data.spent)));
        if (data.goalAmount != null && data.goalAmount > 0) {
            goalAmountText.setText(String.format("%s원", formatter.format(data.goalAmount)));
            goalAmountText.setVisibility(View.VISIBLE);
        } else {
            goalAmountText.setVisibility(View.INVISIBLE);
        }

        Collections.sort(data.items, (o1, o2) -> Long.compare(o2.expense, o1.expense));
        List<CategoryRatioResponse.Item> displayItems = new ArrayList<>();
        if (data.items.size() > 3) {
            displayItems.addAll(data.items.subList(0, 3));
            long etcExpense = 0;
            for (int i = 3; i < data.items.size(); i++) {
                etcExpense += data.items.get(i).expense;
            }
            if (etcExpense > 0) {
                CategoryRatioResponse.Item etcItem = new CategoryRatioResponse.Item();
                etcItem.category = "기타";
                etcItem.expense = etcExpense;
                displayItems.add(etcItem);
            }
        } else {
            displayItems.addAll(data.items);
        }

        if (displayItems.isEmpty()) {
            legendLayout.setVisibility(View.GONE);
            renderStackedGoalBar(data.goalAmount != null ? data.goalAmount : data.spent, data.spent, new int[]{});
            return;
        }

        int[] segments = new int[displayItems.size()];
        for (int i = 0; i < displayItems.size(); i++) {
            segments[i] = (int) displayItems.get(i).expense;
        }

        long goal = (data.goalAmount != null && data.goalAmount > 0) ? data.goalAmount : data.spent;
        renderStackedGoalBar(goal, data.spent, segments);

        legendLayout.setVisibility(View.VISIBLE);
        TextView[] legendTexts = {legendText1, legendText2, legendText3};
        ImageView[] legendIcons = {legendIcon1, legendIcon2, legendIcon3};
        for (int i = 0; i < legendTexts.length; i++) {
            if (i < data.items.size() && i < 3) {
                legendTexts[i].setVisibility(View.VISIBLE);
                legendIcons[i].setVisibility(View.VISIBLE);
                legendTexts[i].setText(data.items.get(i).category);
                legendIcons[i].setColorFilter(CATEGORY_COLORS[i]);
            } else {
                legendTexts[i].setVisibility(View.GONE);
                legendIcons[i].setVisibility(View.GONE);
            }
        }
    }

    private void setupButtonClickListeners(View view) {
        bellBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificationActivity.class)));
        mypageBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), MypageActivity.class)));
        toLedgerBtn.setOnClickListener(v -> navigateToFragment(new MainMenuLedgerFragment()));
        toChallengeBtn.setOnClickListener(v -> navigateToFragment(new MainMenuChallengeFragment()));
        toFinEdBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), FinanceInfoActivity.class)));
        toMobtiBtn.setOnClickListener(v -> startActivity(new Intent(requireContext(), MobtiActivity.class)));
    }

    private void navigateToFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.menu_frame_layout, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void setupNotificationBadge() {
        notificationRepo = new NotificationRepository(requireContext());
        badge = BadgeDrawable.create(requireContext());
        badge.setVisible(false);
        badge.setBadgeGravity(BadgeDrawable.TOP_END);
        badge.clearNumber();
        badge.setHorizontalOffset(dp(5));
        badge.setVerticalOffset(dp(5));
        BadgeUtils.attachBadgeDrawable(badge, bellBtn);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUnreadBadge();
    }

    private void refreshUnreadBadge() {
        notificationRepo.getUnreadCount(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> res) {
                int count = (res.isSuccessful() && res.body() != null) ? res.body() : 0;
                badge.setVisible(count > 0);
                BadgeUtils.attachBadgeDrawable(badge, bellBtn);
            }
            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                badge.setVisible(false);
                BadgeUtils.attachBadgeDrawable(badge, bellBtn);
            }
        });
    }

    private void renderStackedGoalBar(long goal, long spent, int[] segments) {
        if (goalBarTrack == null) return;
        goalBarTrack.removeAllViews();
        LinearLayout spentBar = new LinearLayout(requireContext());
        spentBar.setOrientation(LinearLayout.HORIZONTAL);
        spentBar.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(spent, 1)));
        View remainSpacer = new View(requireContext());
        remainSpacer.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, Math.max(goal - spent, 0)));
        goalBarTrack.addView(spentBar);
        goalBarTrack.addView(remainSpacer);
        long totalSegmentValue = 0;
        for (int v : segments) totalSegmentValue += v;
        if (totalSegmentValue == 0) return;
        for (int i = 0; i < segments.length; i++) {
            int segmentValue = segments[i];
            if (segmentValue <= 0) continue;
            View part = new View(requireContext());
            part.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, segmentValue));
            boolean isFirst = (i == 0);
            boolean isLast = (i == segments.length - 1);
            part.setBackground(makeRoundedSegment(CATEGORY_COLORS[i % CATEGORY_COLORS.length], isFirst, isLast, spent >= goal));
            spentBar.addView(part);
        }
    }

    private GradientDrawable makeRoundedSegment(int color, boolean isFirst, boolean isLast, boolean isExceeded) {
        float r = getResources().getDisplayMetrics().density * 8f;
        float tl = isFirst ? r : 0;
        float bl = isFirst ? r : 0;
        float tr = (isLast && !isExceeded) ? r : 0;
        float br = (isLast && !isExceeded) ? r : 0;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadii(new float[]{tl, tl, tr, tr, br, br, bl, bl});
        return gd;
    }

    private int dp(int dp) {
        float d = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * d);
    }
}