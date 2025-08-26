package com.moneybuddy.moneylog.mypage.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.moneybuddy.moneylog.R;

import com.moneybuddy.moneylog.common.ApiService;
import com.moneybuddy.moneylog.common.RetrofitClient;
import com.moneybuddy.moneylog.login.activity.LoginActivity;
import com.moneybuddy.moneylog.mypage.dto.MobtiBriefDto;
import com.moneybuddy.moneylog.mypage.dto.UserExpResponse;
import com.moneybuddy.moneylog.mypage.dto.PushSettingRequest;
import com.moneybuddy.moneylog.mypage.dto.PushSettingResponse;
import com.moneybuddy.moneylog.mypage.dto.UserDeleteRequest;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MypageActivity extends AppCompatActivity {

    private static final String TAG = "MypageActivity";

    private ImageButton btnBack;

    // 프로필 영역
    private TextView tvEmoji;
    private TextView tvProfileMobtiLabel, tvProfileName, tvProfileMobtiDesc;

    // 레벨 및 계정 정보
    private TextView tvLevelValue, tvEmail;
    private View viewLevelProgress;
    private TextView btnChangePassword, btnLogout, btnWithdrawal;
    private SwitchMaterial switchNotification;

    // 네트워크 서비스
    private ApiService apiService;

    // 스위치 리스너의 무한 루프를 방지하기 위한 플래그
    private boolean isProgrammaticChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // Retrofit ApiService 초기화
        apiService = com.moneybuddy.moneylog.common.RetrofitClient.api(MypageActivity.this);

        // UI 요소 초기화
        initializeViews();

        // 클릭 리스너 설정
        setupClickListeners();

        // 서버에서 실제 데이터 불러옴
        loadServerData();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);

        // MOBTI 관련 UI
        tvEmoji = findViewById(R.id.iv_profile);
        tvProfileMobtiLabel = findViewById(R.id.tv_profile_mobti_label);
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileMobtiDesc = findViewById(R.id.tv_profile_mobti_desc);

        // 계정 관련 UI
        tvLevelValue = findViewById(R.id.tv_level_value);
        viewLevelProgress = findViewById(R.id.view_level_progress);
        tvEmail = findViewById(R.id.tv_email);
        switchNotification = findViewById(R.id.switch_notification);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
        btnWithdrawal = findViewById(R.id.btn_withdrawal);
    }

    // 서버에서 필요한 모든 데이터를 로드
    private void loadServerData() {
        // MOBTI 정보 불러옴
        loadMobtiData();

       // 레벨 및 경험치 정보 불러옴
        loadLevelData();

        // 알림 설정 정보 불러옴
        loadNotificationSetting();

        // 이메일, 레벨 등 다른 정보를 불러오는 API도 이곳에서 호출하기
    }

    // 서버에 MOBTI 요약 정보 요청
    private void loadMobtiData() {
        apiService.getMyMobtiSummary().enqueue(new Callback<MobtiBriefDto>() {
            @Override
            public void onResponse(@NonNull Call<MobtiBriefDto> call, @NonNull Response<MobtiBriefDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateUiWithMobti(response.body());
                } else {
                    Log.e(TAG, "MOBTI 정보 로드 실패: " + response.code());
                    tvProfileName.setText("내 소비 유형은?");
                    tvProfileMobtiDesc.setText("MOBTI 검사를 통해 알아보세요!");
                }
            }

            @Override
            public void onFailure(@NonNull Call<MobtiBriefDto> call, @NonNull Throwable t) {
                Log.e(TAG, "네트워크 오류: ", t);
                Toast.makeText(MypageActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 받아온 MOBTI 데이터로 화면 업데이트
    private void updateUiWithMobti(MobtiBriefDto mobtiData) {
        // code 값으로 영어 알파벳 TextView 설정
        tvProfileMobtiLabel.setText(mobtiData.getCode());
        // 닉네임과 한줄 요약 설정
        tvProfileName.setText(mobtiData.getNickname());
        tvProfileMobtiDesc.setText(mobtiData.getSummary());
        // MOBTI 코드에 맞는 이모지 찾아서 설정
        String emoji = getEmojiForMobtiCode(mobtiData.getCode());
        tvEmoji.setText(emoji);
    }

    // MOBTI 코드에 해당하는 동물 이모지 반환
    private String getEmojiForMobtiCode(String code) {
        if (code == null) return "❔";

        switch (code) {
            case "IMTP": return "🐿️"; case "IMTR": return "🐢";
            case "IMCP": return "🦉"; case "IMCR": return "🐫"; // 황소 -> 낙타로 대체 (다시 바꾸기)
            case "ISTP": return "🐈"; case "ISTR": return "🦩";
            case "ISCP": return "🐕"; case "ISCR": return "🐒";
            case "EMTP": return "🦦"; case "EMTR": return "🦔"; // EMTP 비버 -> 수달로 대체
            case "EMCP": return "🐧"; case "EMCR": return "🐼";
            case "ESTP": return "🦊"; case "ESTR": return "🦅";
            case "ESCP": return "🐘"; case "ESCR": return "🐎";
            default: return "❔";
        }
    }

    // 서버에 레벨 및 경험치 정보 요청
    private void loadLevelData() {
        apiService.getMyExp().enqueue(new Callback<UserExpResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserExpResponse> call, @NonNull Response<UserExpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 요청 성공 시 UI 업데이트
                    updateUiWithLevel(response.body());
                } else {
                    // 요청 실패 시 기본 텍스트 표시
                    Log.e(TAG, "레벨 정보 로드 실패: " + response.code());
                    tvLevelValue.setText("Lv. ?");
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserExpResponse> call, @NonNull Throwable t) {
                // 네트워크 오류 발생 시
                Log.e(TAG, "네트워크 오류 (레벨 정보): ", t);
                Toast.makeText(MypageActivity.this, "레벨 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 받아온 레벨 데이터로 레벨, 경험치 바 업데이트
    private void updateUiWithLevel(UserExpResponse levelData) {
        tvLevelValue.setText("Lv." + levelData.getLevel());

        // 경험치 바 너비 계산 및 설정
        // experience 값을 0~100 사이의 백분율로 정함
        float progressPercentage = levelData.getExperience() / 100.0f;
        View parentView = (View) viewLevelProgress.getParent();
        parentView.post(() -> {
            int parentWidth = parentView.getWidth() - parentView.getPaddingLeft() - parentView.getPaddingRight();
            ViewGroup.LayoutParams params = viewLevelProgress.getLayoutParams();
            params.width = (int) (parentWidth * progressPercentage);
            viewLevelProgress.setLayoutParams(params);
        });
    }

    // 현재 알림 설정 값 스위치에 반영
    private void loadNotificationSetting() {
        String token = getTokenFromPreferences();
        if (token == null) return;

        apiService.getPushSetting("Bearer " + token).enqueue(new Callback<PushSettingResponse>() {
            @Override
            public void onResponse(@NonNull Call<PushSettingResponse> call, @NonNull Response<PushSettingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean isEnabled = response.body().isEnabled();
                    isProgrammaticChange = true;
                    switchNotification.setChecked(isEnabled);
                    isProgrammaticChange = false;
                } else {
                    Log.e(TAG, "알림 설정 로드 실패: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PushSettingResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "네트워크 오류 (알림 설정): ", t);
            }
        });
    }

    // 스위치 상태 변경 시 서버에 업데이트 요청
    private void updateNotificationSetting(boolean isEnabled) {
        String token = getTokenFromPreferences();
        if (token == null) return;

        PushSettingRequest request = new PushSettingRequest(isEnabled);
        apiService.updatePushSetting("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    String message = isEnabled ? "알림이 켜졌습니다." : "알림이 꺼졌습니다.";
                    Toast.makeText(MypageActivity.this, message, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MypageActivity.this, "알림 설정 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "알림 설정 업데이트 실패: " + response.code());
                    isProgrammaticChange = true;
                    switchNotification.setChecked(!isEnabled); // 이전 상태로 복원
                    isProgrammaticChange = false;
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(MypageActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "네트워크 오류 (알림 설정 업데이트): ", t);
                isProgrammaticChange = true;
                switchNotification.setChecked(!isEnabled); // 이전 상태로 복원
                isProgrammaticChange = false;
            }
        });
    }











    private void setupClickListeners() {

        // 뒤로가기 아이콘 선택 시 홈화면으로 돌아감
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // 비밀번호 변경
        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(MypageActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // 알림 설정 토글 스위치
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 코드에 의해 스위치 상태가 변경된 것이 아니라, 사용자가 직접 터치했을 때만 API 호출
            if (!isProgrammaticChange) {
                updateNotificationSetting(isChecked);
            }
        });

        // 로그아웃
        btnLogout.setOnClickListener(v -> {
            // SharedPreferences에서 현재 토큰 가져옴
            String token = getTokenFromPreferences();

            // 토큰이 없으면 그냥 로컬 데이터만 지우고 로그아웃 처리
            if (token == null || token.isEmpty()) {
                performLocalLogout();
                return;
            }

            // 서버에 로그아웃 요청 (토큰 무효화)
            apiService.logout("Bearer " + token).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    // 서버 응답 성공 여부와 관계없이, 클라이언트에서는 항상 로그아웃 처리
                    Log.d(TAG, "Logout API call successful or responded.");
                    performLocalLogout();
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    // 서버 요청 실패(네트워크 오류 등) 시에도, 클라이언트에서는 항상 로그아웃 처리
                    Log.e(TAG, "Logout API call failed.", t);
                    performLocalLogout();
                }
            });
        });

        // 회원 탈퇴
        btnWithdrawal.setOnClickListener(v -> {
            showWithdrawalConfirmDialog();
        });
    }

    // 로그아웃 처리
    private void performLocalLogout() {
        // 기기에 저장된 사용자 정보 삭제
        clearUserData();

        // 로그인 화면으로 이동
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
    }







    // 비밀번호 탈퇴 확인 다이얼로그
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
        // SharedPreferences에서 저장된 토큰 가져옴
        String token = getTokenFromPreferences();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        UserDeleteRequest request = new UserDeleteRequest(password);

        apiService.deleteUser("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // 성공
                if (response.isSuccessful()) {
                    Toast.makeText(MypageActivity.this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                    // 탈퇴 성공 시, 기기에 저장된 데이터를 모두 지우고 로그인 화면으로 이동
                    clearUserData();

                    Intent intent = new Intent(MypageActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // 마이페이지 종료
                } else {
                    // 서버가 에러 응답을 보낸 경우
                    String errorMessage = "오류가 발생했습니다.";
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
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
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