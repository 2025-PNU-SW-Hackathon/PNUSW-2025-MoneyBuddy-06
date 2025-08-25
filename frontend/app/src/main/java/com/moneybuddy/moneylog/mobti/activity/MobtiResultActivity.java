package com.moneybuddy.moneylog.mobti.activity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto;
import com.moneybuddy.moneylog.mobti.repository.MobtiRepository;
import com.moneybuddy.moneylog.mobti.util.MobtiMascot;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MobtiResultActivity extends AppCompatActivity {

    private String code; // ← 필드로 승격

    private TextView tvEmoji, tvCode, tvTitle, sec1Body, sec2Body, sec3Body;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mobti_result);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        tvEmoji  = findViewById(R.id.tvMascotEmoji);
        tvCode   = findViewById(R.id.tvTypeCode);
        tvTitle  = findViewById(R.id.tvSection1Title);
        sec1Body = findViewById(R.id.section1Body);
        sec2Body = findViewById(R.id.section2Body);
        sec3Body = findViewById(R.id.section3Body);
        MaterialButton btn = findViewById(R.id.btnGoHome);

        code = getIntent().getStringExtra("code");
        if (code == null || code.isBlank()) code = "EMCP";

        tvCode.setText(code);
        tvEmoji.setText(MobtiMascot.emoji(code));
        tvTitle.setText("당신은");

        new MobtiRepository(this).mySummary().enqueue(new Callback<MobtiBriefDto>() {
            @Override public void onResponse(Call<MobtiBriefDto> call, Response<MobtiBriefDto> res) {
                if (res.isSuccessful() && res.body() != null) {
                    MobtiBriefDto d = res.body();
                    String nickAnimal = MobtiMascot.nicknameWithAnimal(d.getNickname(), d.getCode());
                    tvTitle.setText(nickAnimal.isEmpty() ? "당신은" : (nickAnimal + "인 당신은"));
                } else {
                    String ani = MobtiMascot.animal(code); // ← 필드 사용
                    tvTitle.setText(ani.equals("?") ? "당신은" : (ani + "인 당신은"));
                }
            }
            @Override public void onFailure(Call<MobtiBriefDto> call, Throwable t) {
                String ani = MobtiMascot.animal(code); // ← 필드 사용
                tvTitle.setText(ani.equals("?") ? "당신은" : (ani + "인 당신은"));
                Toast.makeText(MobtiResultActivity.this, "요약 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });

        sec1Body.setText("• 유행보다 실용을, 안전한 선택을 선호해요.\n• 구매 전 후기를 꼼꼼히 보고 비교해요.");
        sec2Body.setText("• 가성비를 중시하고 예산을 지키는 편이에요.\n• '싸고 좋은' 선택을 찾는 데 자신 있어요.");
        sec3Body.setText("• 친구들 쇼핑 조언을 자주 해요.\n• 금전 관계를 깔끔하게 정리하는 편이에요.");

        btn.setOnClickListener(v -> finish());
    }
}
