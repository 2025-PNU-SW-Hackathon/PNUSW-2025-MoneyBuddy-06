package com.moneybuddy.moneylog.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageButton;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.moneybuddy.moneylog.R;

import com.moneybuddy.moneylog.activity.ChangePasswordActivity;
import com.moneybuddy.moneylog.network.ApiService;
import com.moneybuddy.moneylog.network.RetrofitClient;
import com.moneybuddy.moneylog.dto.UserDeleteRequest;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MypageActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView ivProfile;
    private TextView tvProfileMobtiLabel, tvProfileName, tvProfileMobtiDesc;
    private TextView tvLevelValue, tvEmail;
    private View viewLevelProgress;
    private TextView btnChangePassword, btnLogout, btnWithdrawal;
    private SwitchMaterial switchNotification;
    private ApiService apiService;

    public static class UserData {
        String profileImageUrl;
        String mobtiLabel;
        String name;
        String mobtiDesc;
        int level;
        int currentExp;
        int maxExp;
        String email;
        boolean isNotificationEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        apiService = RetrofitClient.getApiService();

        initializeViews();
        setupClickListeners();
        fetchUserData();

        final View rootView = findViewById(R.id.mypage_root_layout);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
                int topInset = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

                v.setPadding(v.getPaddingLeft(), topInset, v.getPaddingRight(), v.getPaddingBottom());

                return WindowInsetsCompat.CONSUMED;
            });
        }
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        ivProfile = findViewById(R.id.iv_profile);
        tvProfileMobtiLabel = findViewById(R.id.tv_profile_mobti_label);
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileMobtiDesc = findViewById(R.id.tv_profile_mobti_desc);
        tvLevelValue = findViewById(R.id.tv_level_value);
        viewLevelProgress = findViewById(R.id.view_level_progress);
        tvEmail = findViewById(R.id.tv_email);
        switchNotification = findViewById(R.id.switch_notification);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
        btnWithdrawal = findViewById(R.id.btn_withdrawal);
    }

    private void setupClickListeners() {

        // 뒤로가기 아이콘 선택 시 홈화면으로 돌아감
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // 비밀번호 변경
        btnChangePassword.setOnClickListener(v -> {
            // Intent intent = new Intent(this, ChangePasswordActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "비밀번호 변경 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();
        });

        // 알림 설정 토글 스위치
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 알림을 켜는 API를 호출하는 로직 구현하기
                Toast.makeText(this, "알림이 켜졌습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 알림을 끄는 API를 호출하는 로직 구현하기
                Toast.makeText(this, "알림이 꺼졌습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 로그아웃
        btnLogout.setOnClickListener(v -> {
            clearUserData();

            Intent intent = new Intent(this, MainMenuActivity.class);  //MainMenuActivity -> LoginActivity로 변경하기
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
        });

        // 회원 탈퇴
        btnWithdrawal = findViewById(R.id.btn_withdrawal);
        btnWithdrawal.setOnClickListener(v -> {
            showWithdrawalConfirmDialog();
        });
    }

    //백엔드 서버에 사용자 데이터 요청 (현재는 임시 데이터 사용)
    private void fetchUserData() {

        // 테스트용) 임시 데이터를 생성하여 UI 업데이트 시뮬레이션
        UserData dummyData = new UserData();
        dummyData.profileImageUrl = "https://i.pravatar.cc/150?img=32";
        dummyData.mobtiLabel = "MPTI";
        dummyData.name = "가계부 마스터";
        dummyData.mobtiDesc = "한 푼도 허투루 쓰지 않는 완벽주의 절약왕";
        dummyData.level = 50;
        dummyData.currentExp = 75; // 현재 경험치 (75%)
        dummyData.maxExp = 100;    // 최대 경험치 (100)
        dummyData.email = "moneylog@love.me";
        dummyData.isNotificationEnabled = true;

        updateUi(dummyData);
    }

    //서버에서 받아온 데이터로 UI 업데이트
    private void updateUi(UserData userData) {
        Glide.with(this)
                .load(userData.profileImageUrl)
                .circleCrop()
                .into(ivProfile);

        tvProfileMobtiLabel.setText(userData.mobtiLabel);
        tvProfileName.setText(userData.name);
        tvProfileMobtiDesc.setText(userData.mobtiDesc);
        tvEmail.setText(userData.email);

        tvLevelValue.setText("Lv." + userData.level);

        float progressPercentage = (float) userData.currentExp / userData.maxExp;

        viewLevelProgress.post(() -> {
            ViewGroup.LayoutParams params = viewLevelProgress.getLayoutParams();
            int parentWidth = ((View)viewLevelProgress.getParent()).getWidth();
            params.width = (int) (parentWidth * progressPercentage);
            viewLevelProgress.setLayoutParams(params);
        });

        switchNotification.setChecked(userData.isNotificationEnabled);
    }

    // 1차 확인 다이얼로그
    private void showWithdrawalConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("회원 탈퇴")
                .setMessage("정말 탈퇴하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> {
                    showPasswordInputDialog();
                })
                .setNegativeButton("아니오", null)
                .show();
    }

    // 비밀번호 입력 다이얼로그
    private void showPasswordInputDialog() {
        final EditText passwordInput = new EditText(this);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setHint("비밀번호를 입력하세요");

        new AlertDialog.Builder(this)
                .setTitle("본인 확인")
                .setView(passwordInput)
                .setPositiveButton("확인", (dialog, which) -> {
                    String password = passwordInput.getText().toString();
                    if (password.isEmpty()) {
                        Toast.makeText(this, "비밀번호를 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        requestWithdrawal(password);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // Retrofit을 이용해 서버에 실제 탈퇴 요청
    private void requestWithdrawal(String password) {
        // TODO: SharedPreferences에서 저장된 토큰 가져오기
        String token = getTokenFromPreferences();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "로그인 정보가 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserDeleteRequest request = new UserDeleteRequest(password);

        apiService.deleteUser("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // HTTP 200-299 -> 성공
                    Toast.makeText(MypageActivity.this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                    clearUserData();

                    Intent intent = new Intent(MypageActivity.this, MainMenuActivity.class);    //MainMenuActivity -> IntroActivity로 변경하기
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = "알 수 없는 오류가 발생했습니다.";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e("WithdrawalError", "에러 메시지 파싱 실패", e);
                        }
                    }
                    Toast.makeText(MypageActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("WithdrawalFailure", "네트워크 오류", t);
                Toast.makeText(MypageActivity.this, "네트워크에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getTokenFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        return prefs.getString("token", null);
    }

    private void clearUserData() {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
}