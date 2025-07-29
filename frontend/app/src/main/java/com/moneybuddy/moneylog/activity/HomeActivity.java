package com.moneybuddy.moneylog.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.moneybuddy.moneylog.R;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    // <알림>
    Button toNotification;

    //<설정>
    Button toSetting;

    // <소비 분석 그래프>
    Button toLedgerGraph; // 소비 분석 그래프로 가는 버튼
    TextView thisMonth;

    // <MoBTI>
    Button toMobti; // MoBTI로 가는 버튼

    // <챌린지>
    Button toChallenge; // 챌린지로 가는 버튼

    // <금융 교육>
    Button toFinEd; // 금융 교육으로 가는 버튼

    // <AI 저축 플랜>
    Button toSavPlan; // AI 저축 플랜으로 가는 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        toNotification = findViewById(R.id.button2);
        toSetting = findViewById(R.id.button3);
        toLedgerGraph = findViewById(R.id.button4);
        thisMonth = findViewById(R.id.textView1);
        toMobti = findViewById(R.id.button7);
        toChallenge = findViewById(R.id.button5);
        toFinEd = findViewById(R.id.button6);
        toSavPlan = findViewById(R.id.button1);

        // 화면(버튼) 세팅
        setHomePage();

        // 클릭리스너 세팅
        toNotification.setOnClickListener(v -> onToNotificationClick(v));
        toSetting.setOnClickListener(v -> onToSettingClick(v));
        toLedgerGraph.setOnClickListener(v -> onToLedgerGraphClick(v));
        toMobti.setOnClickListener(v -> onToMobtiClick(v));
        toChallenge.setOnClickListener(v -> onToChallengeClick(v));
        toFinEd.setOnClickListener(v -> onToFinEdClick(v));
        toSavPlan.setOnClickListener(v -> onToSavPlanClick(v));
    }

    private void onToNotificationClick(View v) {
        // Intent intent = new Intent(this, );
        // startActivity(intent);
    }

    private void onToSettingClick(View v) {
        // Intent intent = new Intent(this, );
        // startActivity(intent);
    }

    private void onToLedgerGraphClick(View v) {
        // Intent intent = new Intent(this, );
        // startActivity(intent);
    }

    private void onToMobtiClick(View v) {
        // Intent intent = new Intent(this, );
        // startActivity(intent);
    }

    private void onToChallengeClick(View v) {
        // Intent intent = new Intent(this, );
        // startActivity(intent);
    }

    private void onToFinEdClick(View v) {
        // Intent intent = new Intent(this, );
        // startActivity(intent);
    }

    private void onToSavPlanClick(View v) {
        // Intent intent = new Intent(this, );
        // startActivity(intent);
    }

    private void setHomePage() {
        // 소비 분석 버튼 세팅
        // N월 잔액
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1; // 1을 더해야 함
        thisMonth.setText(month + "월 잔액");
    }
}