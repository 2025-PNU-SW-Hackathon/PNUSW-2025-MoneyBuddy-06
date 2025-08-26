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
import com.moneybuddy.moneylog.mobti.dto.request.MobtiSubmitRequest;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiResultDto;
import com.moneybuddy.moneylog.mobti.repository.MobtiRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MobtiSurveyActivity extends AppCompatActivity {

    private static final int TOTAL = 20;

    private LinearProgressIndicator progress;
    private TextView tvProgress;
    private final ChipGroup[] groups = new ChipGroup[TOTAL];

    private MobtiRepository repo;
    private MaterialButton btnSubmit;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mobti_survey);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        progress = findViewById(R.id.progress);
        tvProgress = findViewById(R.id.tvProgress);
        repo = new MobtiRepository(this);

        groups[0]  = findViewById(R.id.q1).findViewById(R.id.chipGroup);
        groups[1]  = findViewById(R.id.q2).findViewById(R.id.chipGroup);
        groups[2]  = findViewById(R.id.q3).findViewById(R.id.chipGroup);
        groups[3]  = findViewById(R.id.q4).findViewById(R.id.chipGroup);
        groups[4]  = findViewById(R.id.q5).findViewById(R.id.chipGroup);
        groups[5]  = findViewById(R.id.q6).findViewById(R.id.chipGroup);
        groups[6]  = findViewById(R.id.q7).findViewById(R.id.chipGroup);
        groups[7]  = findViewById(R.id.q8).findViewById(R.id.chipGroup);
        groups[8]  = findViewById(R.id.q9).findViewById(R.id.chipGroup);
        groups[9]  = findViewById(R.id.q10).findViewById(R.id.chipGroup);
        groups[10] = findViewById(R.id.q11).findViewById(R.id.chipGroup);
        groups[11] = findViewById(R.id.q12).findViewById(R.id.chipGroup);
        groups[12] = findViewById(R.id.q13).findViewById(R.id.chipGroup);
        groups[13] = findViewById(R.id.q14).findViewById(R.id.chipGroup);
        groups[14] = findViewById(R.id.q15).findViewById(R.id.chipGroup);
        groups[15] = findViewById(R.id.q16).findViewById(R.id.chipGroup);
        groups[16] = findViewById(R.id.q17).findViewById(R.id.chipGroup);
        groups[17] = findViewById(R.id.q18).findViewById(R.id.chipGroup);
        groups[18] = findViewById(R.id.q19).findViewById(R.id.chipGroup);
        groups[19] = findViewById(R.id.q20).findViewById(R.id.chipGroup);

        String[] Q = new String[]{
                "비싸더라도 내가 좋아하는 브랜드라면 구매한다.",
                "나는 돈을 써서 물건을 사는 즐거움보다 목표 금액에 가까워지는 통장 잔고를 확인하는 즐거움이 더 크다.",
                "요즘 유행하는 브랜드나 아이템에 관심이 많다.",
                "큰 소비를 하기 전에 반드시 비교 검색을 한다.",
                "나를 위한 작은 사치는 꼭 필요하다고 생각한다.",
                "수익이 생기면 일정 금액을 저축하거나 별도 계좌로 이체하는 습관이 있다.",
                "트렌드보다 나의 스타일을 더 중요하게 여긴다.",
                "소비를 할 땐 즉흥적으로 결정하는 편이다.",
                "먹고 싶은 건 가격이 좀 비싸도 한 번쯤 산다.",
                "꼭 필요하지 않은 물건은 아무리 싸도 잘 사지 않는다.",
                "믿고 사는 브랜드가 있다.",
                "이번 달 소비 계획이나 예산을 미리 세운다.",
                "같은 기능이라면 더 저렴한 제품을 고른다.",
                "\"돈은 쓰라고 있는 거지!\"라는 말에 공감한다.",
                "유행을 즐기기보다 검증된 제품을 선호한다.",
                "갑작스러운 기분 변화나 분위기에 따라 소비 패턴이 달라지는 경우가 많다.",
                "쇼핑할 때 ‘내 기분’보다 ‘필요 여부’를 먼저 생각한다.",
                "적은 돈 아끼기 위해 불편함을 감수하기보다 돈이 더 들더라도 편리함을 추구한다.",
                "친구나 인플루언서의 추천이 구매 결정에 영향을 끼친다.",
                "할인율이나 이벤트성 보상에 이끌려 구매를 결정한다."
        };
        setQuestionText(R.id.q1,  Q[0]);
        setQuestionText(R.id.q2,  Q[1]);
        setQuestionText(R.id.q3,  Q[2]);
        setQuestionText(R.id.q4,  Q[3]);
        setQuestionText(R.id.q5,  Q[4]);
        setQuestionText(R.id.q6,  Q[5]);
        setQuestionText(R.id.q7,  Q[6]);
        setQuestionText(R.id.q8,  Q[7]);
        setQuestionText(R.id.q9,  Q[8]);
        setQuestionText(R.id.q10, Q[9]);
        setQuestionText(R.id.q11, Q[10]);
        setQuestionText(R.id.q12, Q[11]);
        setQuestionText(R.id.q13, Q[12]);
        setQuestionText(R.id.q14, Q[13]);
        setQuestionText(R.id.q15, Q[14]);
        setQuestionText(R.id.q16, Q[15]);
        setQuestionText(R.id.q17, Q[16]);
        setQuestionText(R.id.q18, Q[17]);
        setQuestionText(R.id.q19, Q[18]);
        setQuestionText(R.id.q20, Q[19]);

        for (ChipGroup g : groups) {
            g.setSingleSelection(true);
            g.setSelectionRequired(false);
            g.setOnCheckedStateChangeListener((group, checkedIds) -> updateProgress());
        }
        updateProgress();

        btnSubmit = findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(v -> {
            int answered = getAnsweredCount();
            if (answered < TOTAL) {
                Toast.makeText(this, "아직 " + (TOTAL - answered) + "문항이 남았어요.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> answers = new ArrayList<>(TOTAL);
            for (int i = 0; i < TOTAL; i++) {
                int id = groups[i].getCheckedChipId();
                Chip chip = id == -1 ? null : groups[i].findViewById(id);
                String t = chip == null ? "" : chip.getText().toString().trim();
                String ox = (t.startsWith("O") || t.startsWith("예")) ? "O" : "X";
                answers.add(ox.toUpperCase(Locale.ROOT));
            }

            // --- 제출 ---
            toggleSubmitting(true);
            MobtiSubmitRequest body = new MobtiSubmitRequest(answers);
            repo.submit(body).enqueue(new Callback<MobtiResultDto>() {
                @Override public void onResponse(Call<MobtiResultDto> call, Response<MobtiResultDto> resp) {
                    toggleSubmitting(false);
                    if (!resp.isSuccessful() || resp.body() == null) {
                        Toast.makeText(MobtiSurveyActivity.this, "제출 실패 (" + resp.code() + ")", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String code = resp.body().getMobti();
                    Intent it = new Intent(MobtiSurveyActivity.this, MobtiResultActivity.class);
                    it.putExtra("code", code);
                    startActivity(it);
                    finish();
                }
                @Override public void onFailure(Call<MobtiResultDto> call, Throwable t) {
                    toggleSubmitting(false);
                    Toast.makeText(MobtiSurveyActivity.this, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void toggleSubmitting(boolean submitting) {
        btnSubmit.setEnabled(!submitting);
        btnSubmit.setText(submitting ? "제출 중..." : "검사 완료  >");
    }

    private void updateProgress() {
        int answered = getAnsweredCount();
        int percent = (int) Math.round(answered * 100.0 / TOTAL);
        progress.setProgressCompat(percent, true);
        tvProgress.setText(answered + " / " + TOTAL + " 문항");
    }

    private int getAnsweredCount() {
        int cnt = 0;
        for (ChipGroup g : groups) if (g.getCheckedChipId() != -1) cnt++;
        return cnt;
    }

    private void setQuestionText(int includeRootId, String text) {
        TextView tv = findViewById(includeRootId).findViewById(R.id.tvQuestion);
        tv.setText(text);
    }
}
