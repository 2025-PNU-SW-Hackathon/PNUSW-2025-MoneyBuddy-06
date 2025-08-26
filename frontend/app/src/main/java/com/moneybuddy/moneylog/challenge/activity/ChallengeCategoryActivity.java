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
    private ImageButton challengeBack;
    private RadioGroup radioGroupChallengeType;
    private RadioGroup radioGroupCategory1;
    private RadioGroup radioGroupCategory2;
    private LinearLayout layoutChallengeCategory;
    private Button buttonSubmit;
    private TextView textViewInitialize;

    // in category 라디오 그룹
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
            challengeBack.setOnClickListener(v -> {
                finish();
            });
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

        buttonSubmit.setOnClickListener(v -> {
            ArrayList<String> selectedCategories = new ArrayList<>();
            String selectedCategory = getSelectedCategory();

            if (selectedCategory != null) {
                selectedCategories.add(selectedCategory);
            }

            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra(EXTRA_SELECTED_CATEGORIES, selectedCategories);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });

        // 초기화 버튼
        textViewInitialize.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra(EXTRA_SELECTED_CATEGORIES, new ArrayList<>());
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
}
