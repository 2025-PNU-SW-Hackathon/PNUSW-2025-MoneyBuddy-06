package com.moneybuddy.moneylog.mobti.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.moneybuddy.moneylog.R;

public class MobtiIntroActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_mobti_intro);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> finish());

        MaterialButton btn = findViewById(R.id.btnStart);
        btn.setOnClickListener(v ->
                startActivity(new Intent(this, MobtiSurveyActivity.class)));
    }
}
