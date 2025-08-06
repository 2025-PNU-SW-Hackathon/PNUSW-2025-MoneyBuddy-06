package com.moneybuddy.moneylog.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.moneybuddy.moneylog.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class SignupActivity extends AppCompatActivity {
    private ImageView ivBackArrow;
    private EditText etEmail, etPassword, etPasswordConfirm;
    private Button btnSignup;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ivBackArrow = findViewById(R.id.ic_arrow_back);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        btnSignup = findViewById(R.id.btn_signup);

        ivBackArrow.setOnClickListener(v -> finish());

        btnSignup.setOnClickListener(v -> attemptSignUp());

        setupRetrofit();
    }

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

        registerUser(email, password);
    }

    private void setupRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://server-address.com/") // 실제 API 서버 주소
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    private void registerUser(String email, String password) {
        SignupRequest signupRequest = new SignupRequest(email, password);
        Call<Void> call = apiService.signup(signupRequest);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("회원가입이 완료되었습니다. 로그인 해주세요.");
                    finish();
                } else {
                    showToast("회원가입에 실패했습니다. (에러 코드: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showToast("네트워크 오류가 발생했습니다.");
                Log.e("SignupActivity", "Network error: " + t.getMessage());
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

class SignupRequest {
    private final String email;
    private final String password;

    public SignupRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}

interface ApiService {
    @POST("signup")
    Call<Void> signup(@Body SignupRequest signupRequest);
}