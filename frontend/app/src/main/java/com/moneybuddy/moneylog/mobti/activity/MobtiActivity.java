package com.moneybuddy.moneylog.mobti.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiFullDto;
import com.moneybuddy.moneylog.mobti.repository.MobtiRepository;
import com.moneybuddy.moneylog.mobti.util.MobtiMascot;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MobtiActivity extends AppCompatActivity {

    private TextView tvEmoji, tvCode, tvCodeKr, tvTitle, sec1, sec2, sec3;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mobti);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        tvEmoji  = findViewById(R.id.tvMascotEmoji);
        tvCode   = findViewById(R.id.tvTypeCode);
        tvCodeKr = findViewById(R.id.tvTypeCodeKrname);    // ← 레이아웃에 있음
        tvTitle  = findViewById(R.id.tvSection1Title);
        sec1     = findViewById(R.id.section1Body);
        sec2     = findViewById(R.id.section2Body);
        sec3     = findViewById(R.id.section3Body);

        MaterialButton btn = findViewById(R.id.btnRetakeMobti);
        btn.setOnClickListener(v -> {
            Intent i = new Intent(MobtiActivity.this, MobtiIntroActivity.class);
            i.putExtra("retake", true);
            startActivity(i);
            finish();
        });

        loadDetails();
    }

    private void loadDetails() {
        new MobtiRepository(this).myDetails().enqueue(new Callback<MobtiFullDto>() {
            @Override public void onResponse(Call<MobtiFullDto> call, Response<MobtiFullDto> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(MobtiActivity.this, "MOBTI 정보를 불러오지 못했어요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                MobtiFullDto d = res.body();
                String code = d.getCode();

                // 상단 배지
                tvCode.setText(code);
                tvEmoji.setText(MobtiMascot.emoji(code));

                // 코드 한글명(닉네임+동물명)
                String nickAnimal = MobtiMascot.nicknameWithAnimal(d.getNickname(), code);
                tvCodeKr.setText(nickAnimal.isEmpty() ? "" : nickAnimal);

                // 카드 타이틀
                tvTitle.setText(nickAnimal.isEmpty() ? "당신은" : (nickAnimal + "인 당신은"));

                // 본문
                sec1.setText(joinBullets(d.getDetailTraits()));
                sec2.setText(joinBullets(d.getSpendingTendency()));
                sec3.setText(joinBullets(d.getSocialStyle()));
            }

            @Override public void onFailure(Call<MobtiFullDto> call, Throwable t) {
                Toast.makeText(MobtiActivity.this, "네트워크 오류가 발생했어요.", Toast.LENGTH_SHORT).show();
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
