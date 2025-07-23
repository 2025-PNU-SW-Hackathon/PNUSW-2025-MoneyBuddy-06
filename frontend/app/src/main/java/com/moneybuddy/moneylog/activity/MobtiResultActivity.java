package com.moneybuddy.moneylog.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.moneybuddy.moneylog.ApiService;
import com.moneybuddy.moneylog.MobtiInfo;
import com.moneybuddy.moneylog.MobtiResultData;
import com.moneybuddy.moneylog.R;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MobtiResultActivity extends AppCompatActivity {
    // View 선언
    ImageView mobti_image;
    TextView mobti_name, mobti_explanation;
    Button start_money_log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobti_result);

        mobti_image = findViewById(R.id.imageView2);
        mobti_name = findViewById(R.id.textView2);
        mobti_explanation = findViewById(R.id.textView6);
        start_money_log = findViewById(R.id.button6);

        // Retrofit 객체 생성
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://172.21.170.228:8080/").addConverterFactory(GsonConverterFactory.create()).build();
        ApiService api = retrofit.create(ApiService.class);
        String token = "Bearer " + "수정 필요";

        // 숫자로 된 mobti_result MobtiInfo 객체로 만들기
        MobtiInfo info = MobtiInfo.DATA.get(MobtiActivity.mobti_result);
        if (info != null) {
            // 데이터 객체 생성
            MobtiResultData data = new MobtiResultData(info.getName());

            // 네트워크 요청
            api.sendMobtiResult(token, data).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {
                        //성공 시 - UI 세팅
                        Log.d("MobtiResultActivity", "서버 전송에 성공했습니다.");
                        mobti_image.setImageResource(info.getImageResId());
                        mobti_name.setText(info.getName() + "\n" + info.getName2());
                        mobti_explanation.setText(info.getDesc());
                    } else {
                        //실패 시 - 검사 다시
                        Log.d("MobtiResultActivity", "서버 전송에 실패했습니다.");
                        Toast.makeText(getApplicationContext(), "서버 전송에 실패했습니다", Toast.LENGTH_SHORT).show();

                        // MobtiMainActivity로 이동
                        Intent intent = new Intent(MobtiResultActivity.this, MobtiMainActivity.class);
                        startActivity(intent);
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                    // 네트워크 에러 시
                    Log.d("MobtiResultActivity", "네트워크 오류가 발생했습니다.");
                    Toast.makeText(getApplicationContext(), "네트워크 오류가 발생했습니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                    t.printStackTrace();

                    // MobtiMainActivity로 이동
                    Intent intent = new Intent(MobtiResultActivity.this, MobtiMainActivity.class);
                    startActivity(intent);
                }
            });
        }
    }
}

