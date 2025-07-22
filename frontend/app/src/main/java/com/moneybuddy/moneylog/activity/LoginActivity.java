

package com.moneybuddy.moneylog.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.moneybuddy.moneylog.R;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton; // MaterialButton을 썼다면 MaterialButton으로 변경

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // 우리가 만든 레이아웃

        // XML 뷰 연결
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        // 로그인 버튼 클릭 이벤트
        loginButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 여기서 로그인 로직 실행 (API 호출 or DB 확인)
            if (email.equals("moneylog@love.me") && password.equals("1234")) {
                Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show();

                // TODO: 로그인 성공 후 다음 화면으로 이동
                // Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                // startActivity(intent);
            } else {
                Toast.makeText(this, "이메일 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        TextView tv1 = findViewById(R.id.tvUnderlined1);
        tv1.setText(Html.fromHtml(
                getString(R.string.underlined_text1),
                Html.FROM_HTML_MODE_LEGACY
        ));
        tv1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView tv2 = findViewById(R.id.tvUnderlined2);
        tv2.setText(Html.fromHtml(
                getString(R.string.underlined_text2),
                Html.FROM_HTML_MODE_LEGACY
        ));
        tv2.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
