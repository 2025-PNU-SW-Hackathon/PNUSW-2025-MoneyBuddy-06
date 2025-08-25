package com.moneybuddy.moneylog.mobti.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.moneybuddy.moneylog.R;
import java.util.ArrayList;
import java.util.List;

public class MobtiSurveyActivity extends AppCompatActivity {

    private LinearProgressIndicator progress;
    private TextView tvProgress;
    private MaterialButton btnSubmit;

    private final int[] groupIds = {
            R.id.chipGroup1, R.id.chipGroup2, R.id.chipGroup3, R.id.chipGroup4, R.id.chipGroup5,
            R.id.chipGroup6, R.id.chipGroup7, R.id.chipGroup8, R.id.chipGroup9, R.id.chipGroup10,
            R.id.chipGroup11, R.id.chipGroup12, R.id.chipGroup13, R.id.chipGroup14, R.id.chipGroup15,
            R.id.chipGroup16, R.id.chipGroup17, R.id.chipGroup18, R.id.chipGroup19, R.id.chipGroup20
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mobti_survey);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        progress = findViewById(R.id.progress);
        tvProgress = findViewById(R.id.tvProgress);
        btnSubmit = findViewById(R.id.btnSubmit);

        updateProgress();
        for (int id : groupIds) {
            ChipGroup g = findViewById(id);
            g.setOnCheckedStateChangeListener((group, ids) -> updateProgress());
        }

        btnSubmit.setOnClickListener(v -> {
            List<String> answers = collectAnswers();
            if (answers == null) return; // 미응답 안내 완료
            // 백엔드 연동 전: 임시 코드로 결과 페이지 이동
            Intent it = new Intent(this, MobtiResultActivity.class);
            it.putExtra("code", "EMCP"); // 임시
            startActivity(it);
            finish();
        });
    }

    private void updateProgress() {
        int answered = 0;
        for (int id : groupIds) {
            ChipGroup g = findViewById(id);
            if (g.getCheckedChipId() != -1) answered++;
        }
        int pct = answered * 5; // 20문항 = 100%
        progress.setProgressCompat(pct, true);
        tvProgress.setText(answered + " / 20 문항");
        btnSubmit.setEnabled(answered == 20);
    }

    private List<String> collectAnswers() {
        List<String> answers = new ArrayList<>(20);
        for (int i = 0; i < groupIds.length; i++) {
            ChipGroup g = findViewById(groupIds[i]);
            int checkedId = g.getCheckedChipId();
            if (checkedId == -1) {
                Toast.makeText(this, (i + 1) + "번 문항을 선택해 주세요.", Toast.LENGTH_SHORT).show();
                return null;
            }
            Chip c = findViewById(checkedId);
            answers.add(c.getText().toString().trim().toUpperCase()); // "O" or "X"
        }
        return answers;
    }
}
