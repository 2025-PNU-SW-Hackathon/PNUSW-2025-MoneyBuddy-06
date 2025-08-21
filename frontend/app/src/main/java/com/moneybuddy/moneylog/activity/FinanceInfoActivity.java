package com.moneybuddy.moneylog.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.tabs.TabLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.moneybuddy.moneylog.R;

public class FinanceInfoActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private NestedScrollView nestedScrollView;
    private FloatingActionButton fabScrollToTop;
    private TabLayout tabLayout;
    private View sectionHealthScore, sectionCardNews, sectionQuiz, sectionYouthPolicy;
    private com.google.android.material.button.MaterialButton btnImproveScore;

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

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        nestedScrollView = findViewById(R.id.nested_scroll_view);
        fabScrollToTop = findViewById(R.id.fab_scroll_to_top);

        tabLayout = findViewById(R.id.tab_layout);

        sectionHealthScore = findViewById(R.id.section_health_score);
        sectionCardNews = findViewById(R.id.section_card_news);
        sectionQuiz = findViewById(R.id.section_quiz);
        sectionYouthPolicy = findViewById(R.id.section_youth_policy);

        btnImproveScore = findViewById(R.id.btn_improve_score);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        fabScrollToTop.setOnClickListener(v -> nestedScrollView.smoothScrollTo(0, 0));

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