package com.moneybuddy.moneylog.challenge.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.challenge.dto.ChallengeCardResponse;
import com.moneybuddy.moneylog.challenge.viewmodel.ChallengeViewModel;

public class ChallengeDetailActivity extends AppCompatActivity {
    private ChallengeViewModel viewModel;
    private ChallengeCardResponse challenge;
    private TextView tvTitle, tvDesc, tvPeriod, tvHowto;
    private ImageView ivCategory;
    private Button buttonJoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);
        viewModel = new ViewModelProvider(this).get(ChallengeViewModel.class);
        challenge = (ChallengeCardResponse) getIntent().getSerializableExtra("challenge");

        initializeViews();

        findViewById(R.id.arrow_back).setOnClickListener(v -> finish());
        buttonJoin.setOnClickListener(v -> {
            if (challenge != null) viewModel.joinChallenge(challenge.getChallengeId());
        });

        if (challenge != null) displayDetails(challenge);
        observeViewModel();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tv_title);
        tvDesc = findViewById(R.id.tv_desc);
        tvPeriod = findViewById(R.id.tv_period);
        tvHowto = findViewById(R.id.tv_howto);
        ivCategory = findViewById(R.id.iv_category);
        buttonJoin = findViewById(R.id.button_join);
    }

    private void displayDetails(ChallengeCardResponse c) {
        tvTitle.setText(c.getTitle());
        tvDesc.setText(c.getDescription());
        tvPeriod.setText(c.getGoalPeriod());

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
                case "저축": // 저축 챌린지
                    ivCategory.setImageResource(R.drawable.category_saving);
                    break;
                case "습관": // 습관 챌린지
                    ivCategory.setImageResource(R.drawable.category_habit);
                    break;
                default:
                    ivCategory.setImageResource(R.drawable.category_others);
                    break;
            }
        } else {
            // todo 이미지 변경
            ivCategory.setImageResource(R.drawable.category_others);
        }

        String goalTypeLabel = "금액".equals(c.getGoalType()) ? "목표 금액" : "목표 횟수";
        String isLinkedText = Boolean.TRUE.equals(c.isAccountLinked()) ? "연동됨" : "연동되지 않음";

        String howtoText = "";
        if (c.getCategory() != null) howtoText += "챌린지 유형: " + c.getCategory() + "\n";
        howtoText += goalTypeLabel + ": " + c.getGoalValue() + "\n" + "가계부 연동 체크 여부: " + isLinkedText;
        tvHowto.setText(howtoText);

        if (Boolean.TRUE.equals(c.isJoined())) {
            buttonJoin.setText("참여 중인 챌린지 입니다.");
            buttonJoin.setEnabled(false);
        }
    }

    private void observeViewModel() {
        viewModel.getJoinResult().observe(this, result -> Toast.makeText(this, result, Toast.LENGTH_SHORT).show());
    }
}