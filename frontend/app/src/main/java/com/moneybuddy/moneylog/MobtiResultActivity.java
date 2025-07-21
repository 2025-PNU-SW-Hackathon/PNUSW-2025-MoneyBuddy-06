package com.moneybuddy.moneylog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class MobtiResultActivity extends AppCompatActivity {
    ImageView mobti_image;
    TextView mobti_name, mobti_explanation;
    Button start_money_log;

    Map<Integer, MobtiInfo> mobtiDict = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobti_result);

        mobti_image = findViewById(R.id.imageView2);
        mobti_name = findViewById(R.id.textView2);
        mobti_explanation = findViewById(R.id.textView6);
        start_money_log = findViewById(R.id.button6);

        putMobtiDict(mobtiDict);

        MobtiInfo info = mobtiDict.get(MobtiActivity.mobti_result);
        if (info != null) {
            mobti_image.setImageResource(info.imageResId);
            mobti_name.setText(info.name + "\n" + info.name2);
            mobti_explanation.setText(info.desc);
        }
    }

    void putMobtiDict(Map<Integer, MobtiInfo> m) {
        m.put(0, new MobtiInfo("MPTI", "가계부 마스터", "한 푼도 허투루 쓰지 않는\n완벽주의 절약왕.\n혼자서 재테크 달인", R.drawable.mobti_character_mpti));
        m.put(1, new MobtiInfo("SPTI", "플렉스 플래너", "많이 쓰지만 계획은 세움.\n소비도 기록하는 깔끔형", R.drawable.mobti_character_spti));
        m.put(10, new MobtiInfo("MATI", "즉흥 기록러", "충동구매는 하지만\n나중에 반드시 정산해서 후회 방지", R.drawable.mobti_character_mati));
        m.put(11, new MobtiInfo("SATI", "후회 방지자", "충동구매 후 반드시\n가계부에 기록하며 자신을 관리", R.drawable.mobti_character_sati));
        m.put(100, new MobtiInfo("MPFI", "숨은 재테크 고수", "계획적이지만 기록은 귀찮아 함.\n겉으론 자유인 같지만 속은 철저", R.drawable.mobti_character_mpfi));
        m.put(101, new MobtiInfo("SPFI", "마음가는 대로 전략가", "계획은 세우지만\n기록은 잘 안 하는 자유 전략가", R.drawable.mobti_character_spfi));
        m.put(110, new MobtiInfo("MAFI", "계획 없는 절약러", "충동과 절약의 줄타기.\n혼자만의 소비 철학이 있음", R.drawable.mobti_character_mafi));
        m.put(111, new MobtiInfo("SAFI", "욜로 독립자", "순간의 행복을 최우선시,\n혼자만의 YOLO 철학가", R.drawable.mobti_character_safi));
        m.put(1000, new MobtiInfo("MPTO", "절약 전도사", "계획도 철저, 가계부도 철저,\n비법을 친구들에게 공유하는 교사형", R.drawable.mobti_character_mpto));
        m.put(1001, new MobtiInfo("SPTO", "소비 큐레이터", "계획적 소비를 하고\n친구들에게도 트렌드를 전파", R.drawable.mobti_character_spto));
        m.put(1010, new MobtiInfo("MATO", "플렉스 후 정산러", "YOLO 라이프를 친구들과 즐기는\n감각적 소비러", R.drawable.mobti_character_mato));
        m.put(1011, new MobtiInfo("SATO", "공유형 욜로러", "충동 소비도 기록도,\n친구와 나누는 라이프 공유형", R.drawable.mobti_character_sato));
        m.put(1100, new MobtiInfo("MPFO", "여유로운 절약가", "절약을 즐기되 기록은 자유롭게.\n친구들과 노하우도 잘 나눔", R.drawable.mobti_character_mpfo));
        m.put(1101, new MobtiInfo("SPFO", "트렌디 자유인", "YOLO 라이프를 친구들과\n즐기는 감각적 소비러", R.drawable.mobti_character_spfo));
        m.put(1110, new MobtiInfo("MAFO", "YOLO 절약러", "오늘은 즐기고 내일은 친구들과\n절약 이야기하는 자유로운 영혼", R.drawable.mobti_character_mafo));
        m.put(1111, new MobtiInfo("SAFO", "파티플렉서", "자유로운 소비와 플렉스를\n친구들과 함께 즐기는 사교형", R.drawable.mobti_character_safo));
    }
}

