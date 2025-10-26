package com.moneybuddy.moneylog.finance.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.finance.adapter.CardNewsAdapter;
import com.moneybuddy.moneylog.finance.dto.request.QuizAnswerRequest;
import com.moneybuddy.moneylog.finance.dto.response.KnowledgeResponse;
import com.moneybuddy.moneylog.finance.dto.response.QuizResponse;
import com.moneybuddy.moneylog.finance.dto.response.QuizResultResponse;
import com.moneybuddy.moneylog.finance.dto.response.YouthPolicyResponse;
import com.moneybuddy.moneylog.common.ApiService;

import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinanceInfoActivity extends AppCompatActivity {
    private static final String TAG = "FinanceInfoActivity";

    // UI 요소
    private ImageButton btnBack;
    private NestedScrollView nestedScrollView;
    private FloatingActionButton fabScrollToTop;
    private TabLayout tabLayout;
    private View sectionHealthScore, sectionCardNews, sectionQuiz;
    private LinearLayout sectionYouthPolicy;
    private com.google.android.material.button.MaterialButton btnImproveScore;
    private ViewPager2 viewPagerCardNews;
    private CardNewsAdapter cardNewsAdapter;

    // 퀴즈 관련 UI 요소
    private TextView tvQuizQuestion;
    private ImageButton btnQuizO, btnQuizX;

    // 네트워크 및 데이터
    private ApiService apiService;
    private Long currentQuizId;

    // FinanceInfoActivity.java 내부

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finance_info);

        View mainLayout = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. apiService를 가장 먼저 초기화하여 NullPointerException을 원천적으로 방지합니다.
        apiService = RetrofitClient.api(this);

        // 2. UI 요소들을 초기화하고 리스너를 설정합니다.
        initializeViews();
        setupClickListeners();
        setupCardNewsSection();

        // 3. 필요한 데이터를 각각 한 번씩 로딩합니다. (중복 호출 제거)
        loadCardNewsData();
        loadTodayQuiz();
        loadYouthPolicies();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        nestedScrollView = findViewById(R.id.nested_scroll_view);
        fabScrollToTop = findViewById(R.id.fab_scroll_to_top);
        tabLayout = findViewById(R.id.tab_layout);

        sectionHealthScore = findViewById(R.id.section_health_score);
        sectionCardNews = findViewById(R.id.tv_card_news_title);
        sectionQuiz = findViewById(R.id.section_quiz);
        sectionYouthPolicy = (LinearLayout) findViewById(R.id.section_youth_policy);
        btnImproveScore = findViewById(R.id.btn_improve_score);

        viewPagerCardNews = findViewById(R.id.view_pager_card_news);

        tvQuizQuestion = findViewById(R.id.tv_quiz_question);
        btnQuizO = findViewById(R.id.btn_quiz_o);
        btnQuizX = findViewById(R.id.btn_quiz_x);
    }

    private void loadYouthPolicies() {
        Call<List<YouthPolicyResponse>> call = apiService.getAllYouthPolicies();

        call.enqueue(new Callback<List<YouthPolicyResponse>>() {
            @Override
            public void onResponse(Call<List<YouthPolicyResponse>> call, Response<List<YouthPolicyResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<YouthPolicyResponse> policies = response.body();

                    sectionYouthPolicy.removeAllViews();

                    for (YouthPolicyResponse policy : policies) {
                        TextView policyTextView = new TextView(FinanceInfoActivity.this);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        policyTextView.setLayoutParams(params);
                        policyTextView.setText(policy.getTitle() + " →");
                        policyTextView.setTextColor(ContextCompat.getColor(FinanceInfoActivity.this, android.R.color.black));
                        policyTextView.setTextSize(16f); // 16sp
                        int padding = (int) (8 * getResources().getDisplayMetrics().density); // 8dp
                        policyTextView.setPadding(0, padding, 0, padding);

                        policyTextView.setOnClickListener(v -> {
                            showPolicyDetailsDialog(policy);
                        });

                        sectionYouthPolicy.addView(policyTextView);
                    }
                } else {
                    Log.e(TAG, "Youth Policies Response Error: " + response.code());
                    Toast.makeText(FinanceInfoActivity.this, "청년 정책 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<YouthPolicyResponse>> call, Throwable t) {
                Log.e(TAG, "Youth Policies API Call Failure", t);
                Toast.makeText(FinanceInfoActivity.this, "네트워크 오류로 청년 정책 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPolicyDetailsDialog(YouthPolicyResponse policy) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_policy_details, null);
        builder.setView(dialogView);

        TextView tvTitle = dialogView.findViewById(R.id.tv_policy_title);
        TextView tvDescription = dialogView.findViewById(R.id.tv_policy_description);
        TextView tvPeriod = dialogView.findViewById(R.id.tv_policy_period);
        TextView tvAmount = dialogView.findViewById(R.id.tv_policy_amount);
        TextView tvEligibility = dialogView.findViewById(R.id.tv_policy_eligibility);
        TextView tvBenefit = dialogView.findViewById(R.id.tv_policy_benefit);
        TextView tvMethod = dialogView.findViewById(R.id.tv_policy_method);
        Button btnGoToWebsite = dialogView.findViewById(R.id.btn_go_to_website);

        tvTitle.setText(policy.getTitle());
        tvDescription.setText(policy.getDescription());
        tvPeriod.setText(policy.getApplicationPeriod());
        tvAmount.setText(policy.getAmount());
        tvEligibility.setText(policy.getEligibility());
        tvBenefit.setText(policy.getBenefit());
        tvMethod.setText(policy.getApplicationMethod());

        final AlertDialog dialog = builder.create();

        btnGoToWebsite.setOnClickListener(v -> {
            String url = policy.getUrl();
            if (url != null && !url.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "연결된 웹사이트가 없습니다.", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss(); // 웹사이트로 이동 후 다이얼로그 닫음
        });

        dialog.show();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        fabScrollToTop.setOnClickListener(v -> nestedScrollView.smoothScrollTo(0, 0));
        btnImproveScore.setOnClickListener(v -> scrollToView(sectionQuiz));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        scrollToView(sectionHealthScore);
                        break;
                    case 1:
                        scrollToView(sectionCardNews);
                        break;
                    case 2:
                        scrollToView(sectionQuiz);
                        break;
                    case 3:
                        scrollToView(sectionYouthPolicy);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 선택이 해제되었을 때
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 이미 선택된 탭이 다시 선택되었을 때
                onTabSelected(tab); // 다시 선택해도 스크롤되도록 설정
            }
        });

        btnImproveScore.setOnClickListener(v -> scrollToView(sectionQuiz));

        btnQuizO.setOnClickListener(v -> {
            if (currentQuizId != null) {
                submitQuizAnswer(true); // 'O'는 true
            } else {
                Toast.makeText(this, "퀴즈를 불러오는 중입니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnQuizX.setOnClickListener(v -> {
            if (currentQuizId != null) {
                submitQuizAnswer(false); // 'X'는 false
            } else {
                Toast.makeText(this, "퀴즈를 불러오는 중입니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCardNewsSection() {
        cardNewsAdapter = new CardNewsAdapter();
        viewPagerCardNews.setAdapter(cardNewsAdapter);
        viewPagerCardNews.setOffscreenPageLimit(3);
    }

    private void loadCardNewsData() {
        Call<List<KnowledgeResponse>> call = apiService.getTodayCardNews();

        call.enqueue(new Callback<List<KnowledgeResponse>>() {
            @Override
            public void onResponse(Call<List<KnowledgeResponse>> call, Response<List<KnowledgeResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<KnowledgeResponse> newsList = response.body();
                    cardNewsAdapter.setCardNewsList(newsList);
                } else {
                    Log.e("FinanceInfoActivity", "Response Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<KnowledgeResponse>> call, Throwable t) {
                Log.e("FinanceInfoActivity", "API Call Failure: " + t.getMessage());
            }
        });
    }

    private void loadTodayQuiz() {
        apiService.getTodayQuiz().enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QuizResponse quiz = response.body();
                    tvQuizQuestion.setText(quiz.getQuestion());
                    currentQuizId = quiz.getQuizId(); // 퀴즈 ID 저장
                } else {
                    tvQuizQuestion.setText("오늘의 퀴즈를 불러오지 못했습니다.");
                    Log.e(TAG, "Quiz Response Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<QuizResponse> call, Throwable t) {
                tvQuizQuestion.setText("퀴즈 로딩 실패: 네트워크 연결을 확인하세요.");
                Log.e(TAG, "Quiz API Call Failure", t);
            }
        });
    }

    private void submitQuizAnswer(boolean userAnswer) {
        QuizAnswerRequest request = new QuizAnswerRequest(currentQuizId, userAnswer);

        apiService.submitAnswer(request).enqueue(new Callback<QuizResultResponse>() {
            @Override
            public void onResponse(Call<QuizResultResponse> call, Response<QuizResultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QuizResultResponse result = response.body();
                    showResultDialog(result.isCorrect(), result.getExplanation());
                } else {
                    Toast.makeText(FinanceInfoActivity.this, "정답 제출에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Answer Submit Response Error: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<QuizResultResponse> call, Throwable t) {
                Toast.makeText(FinanceInfoActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Answer Submit API Call Failure", t);
            }
        });
    }

    private void showResultDialog(boolean isCorrect, String explanation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isCorrect ? "🎉 정답입니다!" : "😥 아쉬워요, 오답입니다!");
        builder.setMessage(explanation);
        builder.setPositiveButton("확인", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void scrollToView(View view) {
        view.post(() -> {
            int y = view.getTop();
            int offset = (int) (16 * getResources().getDisplayMetrics().density);
            int scrollTo = Math.max(y - offset, 0);
            nestedScrollView.smoothScrollTo(0, scrollTo);
        });
    }
}