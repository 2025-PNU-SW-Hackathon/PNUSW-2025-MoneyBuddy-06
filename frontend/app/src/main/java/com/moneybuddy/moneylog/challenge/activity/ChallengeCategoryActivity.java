package com.moneybuddy.moneylog.challenge.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.moneybuddy.moneylog.R;

import java.util.ArrayList;

public class ChallengeCategoryActivity extends AppCompatActivity {
    public static final String EXTRA_SELECTED_CATEGORIES = "selected_categories";
    public static final String EXTRA_SELECTED_TYPE = "selected_type"; // 추가: 타입 전달용 키

    private ImageButton challengeBack;
    private RadioGroup radioGroupChallengeType;
    private RadioGroup radioGroupCategory1;
    private RadioGroup radioGroupCategory2;
    private LinearLayout layoutChallengeCategory;
    private Button buttonSubmit;
    private TextView textViewInitialize;

    private boolean isClearing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_category);

        initializeViews();
        setupListeners();

        layoutChallengeCategory.setVisibility(View.VISIBLE);
    }

    private void initializeViews() {
        challengeBack = findViewById(R.id.arrow_back);
        radioGroupChallengeType = findViewById(R.id.radiogroup_challenge_type);
        radioGroupCategory1 = findViewById(R.id.radiogroup_challenge_category1);
        radioGroupCategory2 = findViewById(R.id.radiogroup_challenge_category2);
        layoutChallengeCategory = findViewById(R.id.layout_challenge_category);
        buttonSubmit = findViewById(R.id.button_submit);
        textViewInitialize = findViewById(R.id.textview_initialize);
    }

    private void setupListeners() {
        // 뒤로가기 버튼
        if (challengeBack != null) {
            challengeBack.setOnClickListener(v -> finish());
        }

        // challenge type 라디오 그룹
        radioGroupChallengeType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radiobutton_spending) {
                layoutChallengeCategory.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radiobutton_savings) {
                layoutChallengeCategory.setVisibility(View.GONE);
            }
        });

        // challenge category 라디오 그룹 2행이 하나로 작동하도록 하기 위한 코드
        radioGroupCategory1.setOnCheckedChangeListener((group, checkedId) -> {
            if (isClearing || checkedId == -1) {
                return;
            }
            isClearing = true;
            radioGroupCategory2.clearCheck();
            isClearing = false;
        });

        radioGroupCategory2.setOnCheckedChangeListener((group, checkedId) -> {
            if (isClearing || checkedId == -1) {
                return;
            }
            isClearing = true;
            radioGroupCategory1.clearCheck();
            isClearing = false;
        });

        // 적용 버튼
        buttonSubmit.setOnClickListener(v -> {
            ArrayList<String> selectedCategories = new ArrayList<>();
            String selectedCategory = getSelectedCategory();
            String selectedType = getSelectedType(); // 추가: 타입 가져오기

            if (selectedCategory != null) {
                selectedCategories.add(selectedCategory);
            }

            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra(EXTRA_SELECTED_CATEGORIES, selectedCategories);
            resultIntent.putExtra(EXTRA_SELECTED_TYPE, selectedType); // 타입도 함께 전달
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        // 초기화 버튼
        textViewInitialize.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra(EXTRA_SELECTED_CATEGORIES, new ArrayList<>());
            resultIntent.putExtra(EXTRA_SELECTED_TYPE, (String) null); // 타입 필터도 초기화
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    private String getSelectedCategory() {
        int checkedId1 = radioGroupCategory1.getCheckedRadioButtonId();
        int checkedId2 = radioGroupCategory2.getCheckedRadioButtonId();

        if (checkedId1 != -1) {
            RadioButton selectedRadioButton = findViewById(checkedId1);
            return selectedRadioButton.getText().toString();
        }

        if (checkedId2 != -1) {
            RadioButton selectedRadioButton = findViewById(checkedId2);
            return selectedRadioButton.getText().toString();
        }

        return null;
    }

    // 추가: 타입 문자열로 변환
    private String getSelectedType() {
        int checkedId = radioGroupChallengeType.getCheckedRadioButtonId();

        if (checkedId == R.id.radiobutton_spending) {
            return "지출";
        } else if (checkedId == R.id.radiobutton_savings) {
            return "저축";
        }
        // 습관용 라디오버튼이 따로 있으면 여기에 else if 추가
        // else if (checkedId == R.id.radiobutton_habit) {
        //     return "습관";
        // }

        return null;
    }
}
