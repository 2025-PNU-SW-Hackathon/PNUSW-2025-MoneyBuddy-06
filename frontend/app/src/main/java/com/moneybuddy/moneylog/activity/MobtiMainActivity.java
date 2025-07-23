package com.moneybuddy.moneylog.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moneybuddy.moneylog.R;

public class MobtiMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mobti_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // MoBTI 만 하이라이트 (span)
        TextView textView = findViewById(R.id.textView);
        String content = String.valueOf(textView.getText());
        SpannableString spannable = new SpannableString(content);

        int start = content.indexOf("MoBTI");
        int end = start + "MoBTI".length();

        // substring이 실제로 존재할 때만 하이라이트
        if (start >= 0) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#86935A")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new RelativeSizeSpan(1.5f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        textView.setText(spannable);

        // 검사하러 가기 버튼
        Button button = findViewById(R.id.button);
        button.setOnClickListener(v -> onButtonClick(v));
    }

    public void onButtonClick(View v) {
        // MobtiActivity로 이동
        Intent intent = new Intent(this, MobtiActivity.class);
        startActivity(intent);
    }
}
