package com.moneybuddy.moneylog.mypage.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.common.ApiService;
import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.login.activity.LoginActivity;
import com.moneybuddy.moneylog.mypage.dto.ChangePasswordRequest;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangePasswordActivity";

    // UI 요소
    private ImageButton btnBack;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private Button btnSubmitChange;

    // 네트워크 서비스
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Retrofit ApiService 초기화
        apiService = com.moneybuddy.moneylog.common.RetrofitClient.api(ChangePasswordActivity.this);

        // UI 컴포넌트 초기화
        initializeViews();

        // 클릭 리스너 설정
        setupClickListeners();
    }

    // XML 레이아웃의 뷰들을 코드와 연결
    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmNewPassword = findViewById(R.id.et_confirm_new_password);
        btnSubmitChange = findViewById(R.id.btn_submit_change);
    }

    // 버튼 클릭 이벤트 처리
    private void setupClickListeners() {
        // 뒤로가기 버튼 누르면 현재 화면 종료
        btnBack.setOnClickListener(v -> finish());

        // '변경하기' 버튼 누르면 비밀번호 변경 시도
        btnSubmitChange.setOnClickListener(v -> attemptPasswordChange());
    }

    // 비밀번호 변경 로직 시작
    private void attemptPasswordChange() {
        // 입력된 텍스트 가져옴
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmNewPassword = etConfirmNewPassword.getText().toString().trim();

        // 입력값 유효성 검사
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 서버에 변경 요청 보냄
        requestChangePassword(currentPassword, newPassword);
    }

    // 서버에 비밀번호 변경 요청 전송
    private void requestChangePassword(String currentPassword, String newPassword) {
        String token = getTokenFromPreferences();
        if (token == null) {
            Toast.makeText(this, "로그인 정보가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);

        apiService.changePassword("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // 성공
                if (response.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "비밀번호가 변경되었습니다. 다시 로그인해주세요.", Toast.LENGTH_LONG).show();
                    // 사용자 데이터 지우고 로그인 화면으로 이동
                    clearUserDataAndGoToLogin();
                } else {
                    // 실패
                    String errorMessage = "비밀번호 변경에 실패했습니다.";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "에러 메시지 파싱 실패", e);
                        }
                    }
                    Toast.makeText(ChangePasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "네트워크 오류", t);
                Toast.makeText(ChangePasswordActivity.this, "네트워크에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // SharedPreferences에서 토큰 가져옴
    private String getTokenFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        return prefs.getString("token", null);
    }

    // 비밀번호 변경 성공 후, 저장된 사용자 데이터를 모두 지우고 로그인 화면으로 이동
    private void clearUserDataAndGoToLogin() {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // 로그인 화면으로 이동하고, 이전의 모든 액티비티 스택 제거
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // 현재 액티비티 종료
    }
}