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

import com.moneybuddy.moneylog.signup.activity.SignupActivity;

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
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
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
                showLoading(false);

                auth.saveLogin(data.token, data.userId, data.email);
                Toast.makeText(LoginActivity.this,
                        data.message != null ? data.message : "로그인 성공",
                        Toast.LENGTH_SHORT).show();

                // 로그인 성공 → 홈 화면
                startActivity(new Intent(LoginActivity.this, MainMenuActivity.class));
                finish();
            }

            @Override public void onError(String msg) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (show) {
            if (progress == null) {
                progress = new ProgressDialog(this);
                progress.setMessage("로그인 중...");
                progress.setCancelable(false);
            }
            progress.show();
        } else if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }
}
