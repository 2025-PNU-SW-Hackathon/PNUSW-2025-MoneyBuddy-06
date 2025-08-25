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

    private TextView tvEmoji, tvCode, tvTitle, sec1, sec2, sec3;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mobti);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        tvEmoji = findViewById(R.id.tvMascotEmoji);  // TextView(이모지)
        tvCode  = findViewById(R.id.tvTypeCode);
        tvTitle = findViewById(R.id.tvSection1Title); // "닉네임 동물 인 당신은"
        sec1    = findViewById(R.id.section1Body);
        sec2    = findViewById(R.id.section2Body);
        sec3    = findViewById(R.id.section3Body);

        // 재검사하기 → 바로 인트로로 이동
        MaterialButton btn = findViewById(R.id.btnRetakeMobti);
        btn.setOnClickListener(v -> {
            Intent i = new Intent(MobtiActivity.this, MobtiIntroActivity.class);
            startActivity(i);
            finish();
        });

        loadDetails();
    }

    private void loadDetails() {
        MobtiRepository repo = new MobtiRepository(this);
        repo.myDetails().enqueue(new Callback<MobtiFullDto>() {
            @Override public void onResponse(Call<MobtiFullDto> call, Response<MobtiFullDto> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    Toast.makeText(MobtiActivity.this, "불러오기 실패", Toast.LENGTH_SHORT).show();
                    return;
                }
                MobtiFullDto d = res.body();
                final String code = d.getCode();

                tvCode.setText(code);
                tvEmoji.setText(MobtiMascot.emoji(code));

                // "닉네임(동물) 인 당신은"
                String nickAnimal = MobtiMascot.nicknameWithAnimal(d.getNickname(), code);
                tvTitle.setText((nickAnimal == null || nickAnimal.isEmpty()) ? "당신은" : (nickAnimal + " 인 당신은"));

                sec1.setText(joinBullets(d.getDetailTraits()));
                sec2.setText(joinBullets(d.getSpendingTendency()));
                sec3.setText(joinBullets(d.getSocialStyle()));
            }

            @Override public void onFailure(Call<MobtiFullDto> call, Throwable t) {
                Toast.makeText(MobtiActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
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
