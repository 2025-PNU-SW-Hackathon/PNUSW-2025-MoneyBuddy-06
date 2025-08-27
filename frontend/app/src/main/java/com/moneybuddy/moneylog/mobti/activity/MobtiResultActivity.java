package com.moneybuddy.moneylog.mobti.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.main.activity.MainMenuActivity;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiFullDto;
import com.moneybuddy.moneylog.mobti.repository.MobtiRepository;
import com.moneybuddy.moneylog.mobti.util.MobtiMascot;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MobtiResultActivity extends AppCompatActivity {

    private TextView tvEmoji, tvCode, tvTitle, sec1Body, sec2Body, sec3Body;
    private MaterialButton btnGoHome;
    private TextView tvCodeKrName;
    private String code;                 // 서버/설문에서 받은 코드(없어도 서버 조회 시 채움)
    private boolean fromFirstLogin;      // 첫 로그인에서 검사한 경우 홈으로 보내기

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mobti_result);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        tvEmoji  = findViewById(R.id.tvMascotEmoji);
        tvCode   = findViewById(R.id.tvTypeCode);
        tvCodeKrName = findViewById(R.id.tvTypeCodeKrname);
        tvTitle  = findViewById(R.id.tvSection1Title);
        sec1Body = findViewById(R.id.section1Body);
        sec2Body = findViewById(R.id.section2Body);
        sec3Body = findViewById(R.id.section3Body);
        btnGoHome = findViewById(R.id.btnGoHome);

        // 설문/이전 화면에서 전달된 값
        Intent it = getIntent();
        code = it.getStringExtra("code");
        fromFirstLogin = it.getBooleanExtra("fromFirstLogin", false);

        // 버튼 라벨 & 동작
        if (fromFirstLogin) {
            btnGoHome.setText("머니로그 시작하기");
            btnGoHome.setOnClickListener(v -> {
                Intent home = new Intent(MobtiResultActivity.this, MainMenuActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
                finish();
            });
        } else {
            btnGoHome.setText("홈으로");
            btnGoHome.setOnClickListener(v -> {
                Intent home = new Intent(MobtiResultActivity.this, MainMenuActivity.class);
                home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(home);
                finish();
            });
        }

        // 코드가 넘어왔으면 우선 상단 표현은 셋업
        if (code != null && !code.isBlank()) {
            tvCode.setText(code);
            tvEmoji.setText(MobtiMascot.emoji(code));
        }

        // 상세 설명은 항상 서버에서 조회하여 반영
        loadDetails();
    }

    private void loadDetails() {
        new MobtiRepository(this).myDetails().enqueue(new Callback<MobtiFullDto>() {
            @Override public void onResponse(Call<MobtiFullDto> call, Response<MobtiFullDto> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(MobtiResultActivity.this, "MOBTI 설명을 불러오지 못했어요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                MobtiFullDto d = res.body();

                // 코드/이모지
                String resolvedCode = (d.getCode() != null && !d.getCode().isBlank()) ? d.getCode() : code;
                if (resolvedCode != null) {
                    tvCode.setText(resolvedCode);
                    tvEmoji.setText(MobtiMascot.emoji(resolvedCode));
                }

                String nickAnimal1 = MobtiMascot.nicknameWithAnimal(d.getNickname(), code);
                tvCodeKrName.setText(nickAnimal1.isEmpty() ? "" : nickAnimal1);

                // "닉네임(동물)인 당신은"
                String nickAnimal = MobtiMascot.nicknameWithAnimal(d.getNickname(), resolvedCode);
                tvTitle.setText( (nickAnimal == null || nickAnimal.isEmpty()) ? "당신은" : (nickAnimal + "인 당신은") );

                // 본문 3개 섹션(백엔드에서 내려준 문장 리스트 그대로)
                sec1Body.setText(joinBullets(d.getDetailTraits()));
                sec2Body.setText(joinBullets(d.getSpendingTendency()));
                sec3Body.setText(joinBullets(d.getSocialStyle()));
            }
            @Override public void onFailure(Call<MobtiFullDto> call, Throwable t) {
                Toast.makeText(MobtiResultActivity.this, "MOBTI 설명 불러오기 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String joinBullets(List<String> lines) {
        if (lines == null || lines.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String s : lines) {
            if (s == null) continue;
            String x = s.replaceFirst("^\\s*[-•]\\s*", "").trim();
            if (x.isEmpty()) continue;
            if (sb.length() > 0) sb.append('\n');
            sb.append("• ").append(x);
        }
        return sb.toString();
    }
}
