package com.moneybuddy.moneylog.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.dto.ChallengeCreateRequest;
import com.moneybuddy.moneylog.viewmodel.ChallengeViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ChallengeCreateActivity extends AppCompatActivity {
    private ImageButton challengeBack;
    private AutoCompleteTextView dropdownChallengeType, dropdownSpendingType, dropdownChallengeCategory, dropdownGoalPeriod;
    private TextInputEditText inputChallengeTitle, inputChallengeIntro, inputGoalValue;
    private TextInputLayout layoutSpendingType, layoutChallengeCategory, layoutGoalValue;
    private CheckBox checkboxConfirm, checkboxShare;
    private Button buttonCreateChallenge;
    private TextView textViewGoalValue, textViewSpendingType, textViewChallengeCategory;

    private ChallengeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_create);

        viewModel = new ViewModelProvider(this).get(ChallengeViewModel.class);

        initializeViews();
        setupDropdowns();
        setupListeners();
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getCreateResult().observe(this, result -> {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
            if (result.contains("성공")) finish();
        });
    }

    private void initializeViews() {
        challengeBack = findViewById(R.id.arrow_back);
        dropdownChallengeType = findViewById(R.id.dropdown_challenge_type);
        dropdownSpendingType = findViewById(R.id.dropdown_spending_type);
        dropdownChallengeCategory = findViewById(R.id.dropdown_challenge_category);
        dropdownGoalPeriod = findViewById(R.id.dropdown_goal_period);
        inputChallengeTitle = findViewById(R.id.input_challenge_title);
        inputChallengeIntro = findViewById(R.id.input_challenge_intro);
        inputGoalValue = findViewById(R.id.input_goal_value);
        layoutSpendingType = findViewById(R.id.layout_spending_type);
        layoutChallengeCategory = findViewById(R.id.layout_challenge_category);
        layoutGoalValue = findViewById(R.id.layout_goal_value);
        checkboxConfirm = findViewById(R.id.checkbox_confirm);
        checkboxShare = findViewById(R.id.checkbox_share);
        buttonCreateChallenge = findViewById(R.id.button_create_challenge);
        textViewGoalValue = findViewById(R.id.tv_goal_value);
        textViewSpendingType = findViewById(R.id.tv_spending_type);
        textViewChallengeCategory = findViewById(R.id.tv_challenge_category);
    }

    private void setupDropdowns() {
        List<String> challengeTypes = Arrays.asList("지출 챌린지", "저축 챌린지");
        ArrayAdapter<String> challengeTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, challengeTypes);
        dropdownChallengeType.setAdapter(challengeTypeAdapter);

        List<String> spendingTypes = Arrays.asList("금액", "횟수");
        ArrayAdapter<String> spendingTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, spendingTypes);
        dropdownSpendingType.setAdapter(spendingTypeAdapter);

        List<String> categories = Arrays.asList("식비", "교통", "문화여가", "의료건강", "의류미용", "카페베이커리", "기타");
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        dropdownChallengeCategory.setAdapter(categoryAdapter);

        List<String> periods = Arrays.asList("3일", "7일", "30일");
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, periods);
        dropdownGoalPeriod.setAdapter(periodAdapter);
    }

    private void setupListeners() {
        if (challengeBack != null) {
            challengeBack.setOnClickListener(v -> {
                finish();
            });
        }

        dropdownChallengeType.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                layoutSpendingType.setVisibility(View.VISIBLE);
                layoutChallengeCategory.setVisibility(View.VISIBLE);
                textViewSpendingType.setVisibility(View.VISIBLE);
                textViewChallengeCategory.setVisibility(View.VISIBLE);
            } else {
                layoutSpendingType.setVisibility(View.GONE);
                layoutChallengeCategory.setVisibility(View.GONE);
                textViewSpendingType.setVisibility(View.GONE);
                textViewChallengeCategory.setVisibility(View.GONE);
                textViewGoalValue.setText("목표 금액");
            }
        });

        dropdownSpendingType.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                textViewGoalValue.setText("목표 금액");
                layoutGoalValue.setHelperText("1,000,000 이하의 숫자를 입력하세요.");
            } else {
                textViewGoalValue.setText("목표 횟수");
                String selectedPeriod = dropdownGoalPeriod.getText().toString().replaceAll("[^0-9]", "");
                if (!selectedPeriod.isEmpty()) {
                    layoutGoalValue.setHelperText(selectedPeriod + "보다 작은 숫자를 입력하세요.");
                } else {
                    layoutGoalValue.setHelperText("목표 기간보다 작은 숫자를 입력하세요.");
                }
            }
        });

        buttonCreateChallenge.setOnClickListener(v -> {
            if (validateInput()) {
                sendChallengeDataToServer();
            }
        });
    }

    private void sendChallengeDataToServer() {
        String title = inputChallengeTitle.getText().toString();
        String description = inputChallengeIntro.getText().toString();
        String type = dropdownChallengeType.getText().toString();
        String goalPeriod = dropdownGoalPeriod.getText().toString();
        int goalValue = Integer.parseInt(inputGoalValue.getText().toString());
        Boolean isShared = checkboxShare.isChecked();

        String category = null;
        String goalType = null;

        if (type.equals("지출 챌린지")) {
            category = dropdownChallengeCategory.getText().toString();
            goalType = dropdownSpendingType.getText().toString();
        }

        ChallengeCreateRequest request = new ChallengeCreateRequest(
                title, description, category, type, goalPeriod, goalType, goalValue, isShared
        );

        viewModel.createChallenge(request);
    }

    private boolean validateInput() {
        if (Objects.equals(dropdownChallengeType.getText().toString(), "지출 챌린지")) {
            if (TextUtils.isEmpty(dropdownSpendingType.getText())) {
                Toast.makeText(this, "지출 챌린지 유형을 선택해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (TextUtils.isEmpty(dropdownChallengeCategory.getText())) {
                Toast.makeText(this, "챌린지 카테고리를 선택해주세요.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (TextUtils.isEmpty(inputChallengeTitle.getText()) ||
                TextUtils.isEmpty(inputChallengeIntro.getText()) ||
                TextUtils.isEmpty(dropdownGoalPeriod.getText()) ||
                TextUtils.isEmpty(inputGoalValue.getText())) {
            Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        int goalValue;
        try {
            goalValue = Integer.parseInt(inputGoalValue.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "목표는 숫자로만 입력해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Objects.equals(dropdownSpendingType.getText().toString(), "금액")) {
            if (goalValue > 1000000) {
                Toast.makeText(this, "목표 금액은 1,000,000을 초과할 수 없어요.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else if (Objects.equals(dropdownSpendingType.getText().toString(), "횟수")) {
            String periodStr = dropdownGoalPeriod.getText().toString().replaceAll("[^0-9]", "");
            if (!periodStr.isEmpty()) {
                int period = Integer.parseInt(periodStr);
                if (goalValue >= period) {
                    Toast.makeText(this, "목표 횟수는 목표 기간보다 작아야 해요.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }

        if (!checkboxConfirm.isChecked()) {
            Toast.makeText(this, "안내 문구에 동의해주세요.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
