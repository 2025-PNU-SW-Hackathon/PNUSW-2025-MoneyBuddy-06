package com.moneybuddy.moneylog.login.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.login.dto.LoginResponse;
import com.moneybuddy.moneylog.login.network.AuthRepository;
import com.moneybuddy.moneylog.main.activity.MainMenuActivity;
import com.moneybuddy.moneylog.util.AuthManager;

import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.mobti.repository.MobtiRepository;
import com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto;
import com.moneybuddy.moneylog.mobti.activity.MobtiIntroActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private AuthManager auth;
    private final AuthRepository repo = new AuthRepository();
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setText(Html.fromHtml(getString(R.string.underlined_text2), Html.FROM_HTML_MODE_LEGACY));
        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, com.moneybuddy.moneylog.signup.activity.SignupActivity.class));
        });

        // ---- 로그인 UI ----
        auth = new AuthManager(this);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String email = emailInput.getText().toString().trim();
        String pw = passwordInput.getText().toString();

        if (email.isEmpty()) { emailInput.setError("이메일을 입력해주세요"); return; }
        if (pw.isEmpty())    { passwordInput.setError("비밀번호를 입력해주세요"); return; }

        showLoading(true);

        repo.login(this, email, pw, new AuthRepository.LoginCallback() {
            @Override public void onSuccess(LoginResponse data) {
                // 1) 로컬 로그인 상태 저장
                auth.saveLogin(data.token, data.userId, data.email);

                // 2) Retrofit 인증용 토큰 저장 (인터셉터에서 사용)
                try {
                    TokenManager.getInstance(LoginActivity.this).setToken(data.token);
                } catch (Throwable ignore) {
                    // 혹시 setToken 이름이 다르면 AuthManager가 저장한 토큰을 인터셉터가 읽도록 구현돼 있을 수 있음
                }

                Toast.makeText(LoginActivity.this,
                        data.message != null ? data.message : "로그인 성공",
                        Toast.LENGTH_SHORT).show();

                // 3) 로그인 직후 MOBTI 요약 조회 → 없으면 검사 인트로, 있으면 홈
                checkMobtiAndRoute();
            }

            @Override public void onError(String msg) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    /** 로그인 성공 후 MoBTI 요약 조회 -> 분기 */
    private void checkMobtiAndRoute() {
        new com.moneybuddy.moneylog.mobti.repository.MobtiRepository(this).mySummary()
                .enqueue(new retrofit2.Callback<com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto>() {
                    @Override public void onResponse(retrofit2.Call<com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto> c,
                                                     retrofit2.Response<com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto> r) {
                        boolean hasMobti = r.isSuccessful() && r.body() != null
                                && r.body().getCode() != null && !r.body().getCode().isEmpty();
                        if (hasMobti) {
                            startActivity(new Intent(LoginActivity.this, com.moneybuddy.moneylog.main.activity.MainMenuActivity.class));
                            finish();
                        } else {
                            Intent it = new Intent(LoginActivity.this, com.moneybuddy.moneylog.mobti.activity.MobtiIntroActivity.class);
                            it.putExtra("fromFirstLogin", true);
                            startActivity(it);
                            finish();
                        }
                    }
                    @Override public void onFailure(retrofit2.Call<com.moneybuddy.moneylog.mobti.dto.response.MobtiBriefDto> c, Throwable t) {
                        Toast.makeText(LoginActivity.this, "MOBTI 확인 실패: 네트워크 오류", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goHome() {
        startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
        finish();
    }

    private void goMobtiIntro() {
        startActivity(new Intent(LoginActivity.this, MobtiIntroActivity.class));
        finish();
    }

    private void showLoading(boolean show) {
        if (show) {
            if (progress == null) {
                progress = new ProgressDialog(this);
                progress.setMessage("로그인 중...");
                progress.setCancelable(false);
            }
            if (!progress.isShowing()) progress.show();
        } else if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }
}
