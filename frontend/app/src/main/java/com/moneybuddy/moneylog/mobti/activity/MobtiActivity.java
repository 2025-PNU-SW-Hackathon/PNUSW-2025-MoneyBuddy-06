package com.moneybuddy.moneylog.mobti.activity;

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

import com.moneybuddy.moneylog.mobti.util.MobtiMascot;

public class MobtiActivity extends AppCompatActivity {

    private TextView tvEmoji, tvCode, tvTitle, sec1, sec2, sec3;  // **추가: tvTitle**

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mobti);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        tvEmoji = findViewById(R.id.tvMascotEmoji);
        tvCode  = findViewById(R.id.tvTypeCode);
        tvTitle = findViewById(R.id.tvSection1Title);             // **추가**
        sec1    = findViewById(R.id.section1Body);
        sec2    = findViewById(R.id.section2Body);
        sec3    = findViewById(R.id.section3Body);

        MaterialButton btn = findViewById(R.id.btnGoHome);
        btn.setOnClickListener(v -> finish());

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
                String code = d.getCode();

                tvCode.setText(code);
                tvEmoji.setText(MobtiMascot.emoji(code));

                // "닉네임 동물" + "인 당신은"
                String nickAnimal = MobtiMascot.nicknameWithAnimal(d.getNickname(), code);
                if (nickAnimal.isEmpty()) nickAnimal = "당신은";
                else nickAnimal = nickAnimal + "인 당신은";
                tvTitle.setText(nickAnimal);

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
