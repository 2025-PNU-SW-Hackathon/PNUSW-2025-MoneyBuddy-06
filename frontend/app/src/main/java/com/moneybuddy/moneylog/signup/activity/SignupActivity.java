package com.moneybuddy.moneylog.signup.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.common.ApiService;
import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.signup.dto.UserSignupRequest;
import com.moneybuddy.moneylog.signup.dto.UserSignupResponse;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SignupActivity extends AppCompatActivity {
    private ImageView ivBackArrow;
    private EditText etEmail, etPassword, etPasswordConfirm;
    private Button btnSignup;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // UI 요소 초기화
        ivBackArrow = findViewById(R.id.ic_arrow_back);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        btnSignup = findViewById(R.id.btn_signup);

        apiService = com.moneybuddy.moneylog.common.RetrofitClient.api(SignupActivity.this);


        // 뒤로가기 버튼 클릭
        ivBackArrow.setOnClickListener(v -> finish());

        // 회원가입 버튼 클릭 시 유효성 검사 및 API 호출
        btnSignup.setOnClickListener(v -> attemptSignUp());
    }

    // 회원가입 시도
    private void attemptSignUp() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty()) {
            showToast("모든 정보를 입력해주세요.");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("올바른 이메일 형식을 입력해주세요.");
            return;
        }

        if (password.length() < 8) {
            showToast("비밀번호를 8자 이상 입력하세요.");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(passwordConfirm)) {
            showToast("비밀번호가 일치하지 않습니다.");
            etPasswordConfirm.requestFocus();
            return;
        }

        // 서버에 회원가입 요청
        registerUser(email, password);
    }

    // 서버에 실제 회원가입 요청
    private void registerUser(String email, String password) {
        UserSignupRequest signupRequest = new UserSignupRequest(email, password);
        Call<UserSignupResponse> call = apiService.signup(signupRequest);

        call.enqueue(new Callback<UserSignupResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserSignupResponse> call, @NonNull Response<UserSignupResponse> response) {
                // HTTP 통신 성공
                if (response.isSuccessful() && response.body() != null) {
                    UserSignupResponse signupResponse = response.body();
                    if ("success".equals(signupResponse.getStatus())) {
                        // 회원가입 성공
                        showToast(signupResponse.getMessage());
//                        // 로그인 화면으로 이동
//                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
//                        startActivity(intent);
//                        finish(); // 현재 액티비티 종료
                    } else {
                        // 실패 메시지 띄움
                        showToast(signupResponse.getMessage());
                    }
                } else {
                    // HTTP 통신은 성공했으나, 서버에서 4xx, 5xx 에러 응답
                    try {
                        if (response.errorBody() != null) {
                            String errorBodyString = response.errorBody().string();
                            UserSignupResponse errorResponse = new Gson().fromJson(errorBodyString, UserSignupResponse.class);
                            showToast(errorResponse.getMessage());
                        } else {
                            showToast("회원가입에 실패했습니다. (에러 코드: " + response.code() + ")");
                        }
                    } catch (IOException e) {
                        Log.e("SignupActivity", "Error parsing error body", e);
                        showToast("오류가 발생했습니다.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserSignupResponse> call, @NonNull Throwable t) {
                // 네트워크 연결 실패 등
                showToast("네트워크 오류가 발생했습니다.");
                Log.e("SignupActivity", "Network error: " + t.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}