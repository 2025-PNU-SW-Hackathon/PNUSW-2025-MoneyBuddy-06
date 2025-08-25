package com.moneybuddy.moneylog.mobti.activity;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.moneybuddy.moneylog.R;

public class MobtiResultActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mobti_result);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        ShapeableImageView img = findViewById(R.id.imgMascot);
        TextView tvCode = findViewById(R.id.tvTypeCode);
        TextView sec1Body = findViewById(R.id.section1Body);
        TextView sec2Body = findViewById(R.id.section2Body);
        TextView sec3Body = findViewById(R.id.section3Body);
        MaterialButton btn = findViewById(R.id.btnGoHome);

        String code = getIntent().getStringExtra("code");
        if (code == null) code = "EMCP";
        tvCode.setText(code);
        img.setImageResource(R.drawable.ic_mascot_placeholder);

        sec1Body.setText("• 유행보다 실용을, 안전한 선택을 선호해요.\n• 구매 전 후기를 꼼꼼히 보고 비교해요.");
        sec2Body.setText("• 가성비를 중시하고 예산을 지키는 편이에요.\n• '싸고 좋은' 선택을 찾는 데 자신 있어요.");
        sec3Body.setText("• 친구들 쇼핑 조언을 자주 해요.\n• 금전 관계를 깔끔하게 정리하는 편이에요.");

        btn.setOnClickListener(v -> finish());
    }
}
