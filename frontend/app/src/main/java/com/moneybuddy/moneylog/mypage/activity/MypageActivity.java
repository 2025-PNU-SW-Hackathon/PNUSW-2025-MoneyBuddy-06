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
import com.moneybuddy.moneylog.common.TokenManager;
import com.moneybuddy.moneylog.login.activity.LoginActivity;
import com.moneybuddy.moneylog.mypage.dto.MobtiBriefDto;
import com.moneybuddy.moneylog.mypage.dto.UserExpResponse;
import com.moneybuddy.moneylog.mypage.dto.PushSettingRequest;
import com.moneybuddy.moneylog.mypage.dto.PushSettingResponse;
import com.moneybuddy.moneylog.mypage.dto.UserDeleteRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    private View viewLevelTrack;
    private TextView btnChangePassword, btnLogout, btnWithdrawal;
    private SwitchMaterial switchNotification;

    private ApiService apiService;

    private boolean isProgrammaticChange = false;

    // MOBTI 이모지 + 동물 한글명

    private static final class MobtiLocal {
        final String emoji;
        final String animal;
        MobtiLocal(String e, String a) { emoji = e; animal = a; }
    }
    private static final Map<String, MobtiLocal> MOBTI_MAP = new HashMap<>();
    static {
        // I 계열
        MOBTI_MAP.put("IMTP", new MobtiLocal("🐿️","다람쥐"));
        MOBTI_MAP.put("IMTR", new MobtiLocal("🐢","거북이"));
        MOBTI_MAP.put("IMCP", new MobtiLocal("🦉","부엉이"));
        MOBTI_MAP.put("IMCR", new MobtiLocal("🐫","낙타"));
        MOBTI_MAP.put("ISTP", new MobtiLocal("🐈","고양이"));
        MOBTI_MAP.put("ISTR", new MobtiLocal("🦩","홍학"));
        MOBTI_MAP.put("ISCP", new MobtiLocal("🐕","강아지"));
        MOBTI_MAP.put("ISCR", new MobtiLocal("🐒","원숭이"));
        // E 계열
        MOBTI_MAP.put("EMTP", new MobtiLocal("🦦","수달"));
        MOBTI_MAP.put("EMTR", new MobtiLocal("🦔","고슴도치"));
        MOBTI_MAP.put("EMCP", new MobtiLocal("🐧","펭귄"));
        MOBTI_MAP.put("EMCR", new MobtiLocal("🐼","판다"));
        MOBTI_MAP.put("ESTP", new MobtiLocal("🦊","여우"));
        MOBTI_MAP.put("ESTR", new MobtiLocal("🦅","독수리"));
        MOBTI_MAP.put("ESCP", new MobtiLocal("🐘","코끼리"));
        MOBTI_MAP.put("ESCR", new MobtiLocal("🐎","말"));
    }
    private MobtiLocal dict(String code) {
        if (code == null) return new MobtiLocal("❔","나만의 타입");
        MobtiLocal d = MOBTI_MAP.get(code.toUpperCase());
        return (d != null) ? d : new MobtiLocal("❔","나만의 타입");
    }
    // ─────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        apiService = RetrofitClient.api(MypageActivity.this);
        initializeViews();
        loadUserDataFromPreferences();
        setupClickListeners();
        loadServerData();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        tvEmoji = findViewById(R.id.iv_profile);
        tvProfileMobtiLabel = findViewById(R.id.tv_profile_mobti_label);
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileMobtiDesc = findViewById(R.id.tv_profile_mobti_desc);

        tvLevelValue = findViewById(R.id.tv_level_value);
        viewLevelProgress = findViewById(R.id.view_level_progress);
        //viewLevelTrack = findViewById(R.id.view_level_track);
        tvEmail = findViewById(R.id.tv_email);
        switchNotification = findViewById(R.id.switch_notification);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnLogout = findViewById(R.id.btn_logout);
        btnWithdrawal = findViewById(R.id.btn_withdrawal);
    }

    private void loadServerData() {
        loadMobtiData();
        loadLevelData();
        loadNotificationSetting();
    }

    // ── MOBTI 요약 불러오기
    private void loadMobtiData() {
        apiService.getMyMobtiSummary().enqueue(new Callback<MobtiBriefDto>() {
            @Override public void onResponse(@NonNull Call<MobtiBriefDto> call, @NonNull Response<MobtiBriefDto> res) {
                if (res.isSuccessful() && res.body() != null) {
                    updateUiWithMobti(res.body());
                } else {
                    Log.e(TAG, "MOBTI 로드 실패: " + res.code());
                    updateUiWithMobti(null); // 기본값
                }
            }
            @Override public void onFailure(@NonNull Call<MobtiBriefDto> call, @NonNull Throwable t) {
                Log.e(TAG, "네트워크 오류", t);
                Toast.makeText(MypageActivity.this, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                updateUiWithMobti(null); // 기본값
            }
        });
    }

    // ── UI 업데이트: 이모지/동물명은 로컬, 한줄설명은 서버값 사용
    private void updateUiWithMobti(MobtiBriefDto mobti) {
        String code = (mobti != null) ? mobti.getCode() : null;
        MobtiLocal local = dict(code);

        tvProfileMobtiLabel.setText(code == null ? "MOBTI" : code);
        tvEmoji.setText(local.emoji);                    // 이모지
        tvProfileName.setText(local.animal);             // 동물 한글명

        // 한줄 설명(서버) – 비어있으면 안내 문구로 대체
        String summary = (mobti != null && mobti.getSummary() != null && !mobti.getSummary().isBlank())
                ? mobti.getSummary()
                : "MOBTI 검사를 통해 나의 소비 유형을 확인해보세요!";
        tvProfileMobtiDesc.setText(summary);
    }

    // ── 레벨/경험치
    private void loadLevelData() {
        apiService.getMyExp().enqueue(new Callback<UserExpResponse>() {
            @Override public void onResponse(@NonNull Call<UserExpResponse> call, @NonNull Response<UserExpResponse> res) {
                if (res.isSuccessful() && res.body() != null) {
                    UserExpResponse data = res.body();
                    Log.d(TAG, "서버 응답 성공: Level = " + data.getLevel() + ", Experience = " + data.getExperience());
                    updateUiWithLevel(res.body());
                } else {
                    Log.e(TAG, "레벨 로드 실패: " + res.code());
                    updateUiWithDefaultLevel();
                }
            }
            @Override public void onFailure(@NonNull Call<UserExpResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "레벨 네트워크 오류", t);
                Toast.makeText(MypageActivity.this, "레벨 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                updateUiWithDefaultLevel();
            }
        });
    }

    private void updateUiWithLevel(UserExpResponse d) {
        tvLevelValue.setText("Lv." + d.getLevel());
        float p = d.getExperience() / 100.0f;
//        viewLevelTrack.post(() -> {
//            int trackWidth = viewLevelTrack.getWidth();
//
//            ViewGroup.LayoutParams lp = viewLevelProgress.getLayoutParams();
//            lp.width = (int) (trackWidth * p);
//            viewLevelProgress.setLayoutParams(lp);
//        });
    }

    private void updateUiWithDefaultLevel() {
        tvLevelValue.setText("Lv. 1");
        ViewGroup.LayoutParams lp = viewLevelProgress.getLayoutParams();
        lp.width = 0;
        viewLevelProgress.setLayoutParams(lp);
    }

    // ── 이메일
    private void loadUserDataFromPreferences() {
        TokenManager tokenManager = TokenManager.getInstance(getApplicationContext());
        String userEmail = tokenManager.getEmail();
        if (userEmail == null) {
            userEmail = "정보 없음";
        }
        Log.d(TAG, "TokenManager에서 읽은 이메일: " + userEmail);
        tvEmail.setText(userEmail);
    }

    // ── 알림 설정
    private void loadNotificationSetting() {
        String token = getTokenFromPreferences();
        if (token == null) return;

        apiService.getPushSetting("Bearer " + token).enqueue(new Callback<PushSettingResponse>() {
            @Override public void onResponse(@NonNull Call<PushSettingResponse> c, @NonNull Response<PushSettingResponse> r) {
                if (r.isSuccessful() && r.body() != null) {
                    isProgrammaticChange = true;
                    switchNotification.setChecked(r.body().isEnabled());
                    isProgrammaticChange = false;
                } else {
                    Log.e(TAG, "알림 설정 로드 실패: " + r.code());
                }
            }
            @Override public void onFailure(@NonNull Call<PushSettingResponse> c, @NonNull Throwable t) {
                Log.e(TAG, "알림 설정 네트워크 오류", t);
            }
        });
    }

    private void updateNotificationSetting(boolean isEnabled) {
        String token = getTokenFromPreferences();
        if (token == null) return;

        apiService.updatePushSetting("Bearer " + token, new PushSettingRequest(isEnabled))
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(@NonNull Call<Void> c, @NonNull Response<Void> r) {
                        if (r.isSuccessful()) {
                            Toast.makeText(MypageActivity.this, isEnabled ? "알림이 켜졌습니다." : "알림이 꺼졌습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MypageActivity.this, "알림 설정 변경 실패", Toast.LENGTH_SHORT).show();
                            isProgrammaticChange = true;
                            switchNotification.setChecked(!isEnabled);
                            isProgrammaticChange = false;
                        }
                    }
                    @Override public void onFailure(@NonNull Call<Void> c, @NonNull Throwable t) {
                        Toast.makeText(MypageActivity.this, "네트워크 오류", Toast.LENGTH_SHORT).show();
                        isProgrammaticChange = true;
                        switchNotification.setChecked(!isEnabled);
                        isProgrammaticChange = false;
                    }
                });
    }

    private void setupClickListeners() {
        // 프로필 카드 전체를 탭하면 MobtiActivity 로 이동
        View card = findViewById(R.id.card_profile);
        if (card != null) {
            card.setClickable(true);
            card.setFocusable(true);
            card.setOnClickListener(v -> openMobti());
        }

        int[] tappables = {
                R.id.iv_profile,
                R.id.tv_profile_mobti_label,
                R.id.tv_profile_name,
                R.id.tv_profile_mobti_desc
        };
        View.OnClickListener go = v -> openMobti();
        for (int id : tappables) {
            View t = findViewById(id);
            if (t != null) t.setOnClickListener(go);
        }
        findViewById(R.id.card_profile).setOnClickListener(v -> {
            Intent i = new Intent(MypageActivity.this,
                    com.moneybuddy.moneylog.mobti.activity.MobtiActivity.class);

        });

        btnBack.setOnClickListener(v -> finish());

        btnChangePassword.setOnClickListener(v ->
                startActivity(new Intent(MypageActivity.this, ChangePasswordActivity.class)));

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isProgrammaticChange) updateNotificationSetting(isChecked);
        });

        btnLogout.setOnClickListener(v -> {
            String token = getTokenFromPreferences();
            if (token == null || token.isEmpty()) { performLocalLogout(); return; }
            apiService.logout("Bearer " + token).enqueue(new Callback<Void>() {
                @Override public void onResponse(@NonNull Call<Void> c, @NonNull Response<Void> r) { performLocalLogout(); }
                @Override public void onFailure(@NonNull Call<Void> c, @NonNull Throwable t) { performLocalLogout(); }
            });
        });

        btnWithdrawal.setOnClickListener(v -> showWithdrawalConfirmDialog());
    }

    private void performLocalLogout() {
        clearUserData();
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void showWithdrawalConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("회원 탈퇴")
                .setMessage("정말 탈퇴하시겠습니까?")
                .setPositiveButton("예", (d, w) -> showPasswordInputDialog())
                .setNegativeButton("아니오", null)
                .show();
    }

    private void showPasswordInputDialog() {
        final EditText et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        et.setHint("비밀번호를 입력하세요");

        new AlertDialog.Builder(this)
                .setTitle("본인 확인")
                .setView(et)
                .setPositiveButton("확인", (dialog, which) -> {
                    String pw = et.getText().toString();
                    if (pw.isEmpty()) Toast.makeText(this, "비밀번호를 입력해야 합니다.", Toast.LENGTH_SHORT).show();
                    else requestWithdrawal(pw);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void requestWithdrawal(String password) {
        String token = getTokenFromPreferences();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.deleteUser("Bearer " + token, new UserDeleteRequest(password))
                .enqueue(new Callback<Void>() {
                    @Override public void onResponse(@NonNull Call<Void> c, @NonNull Response<Void> r) {
                        if (r.isSuccessful()) {
                            Toast.makeText(MypageActivity.this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            clearUserData();
                            Intent i = new Intent(MypageActivity.this, LoginActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                            finish();
                        } else {
                            String msg = "오류가 발생했습니다.";
                            try { if (r.errorBody() != null) msg = r.errorBody().string(); } catch (IOException ignored) {}
                            Toast.makeText(MypageActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override public void onFailure(@NonNull Call<Void> c, @NonNull Throwable t) {
                        Toast.makeText(MypageActivity.this, "네트워크에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getTokenFromPreferences() {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        Log.d("TOKEN_CHECK", "SharedPreferences token = " + token);
        return token;
    }

    private void clearUserData() {
        SharedPreferences prefs = getSharedPreferences("auth", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private void openMobti() {
        Log.d(TAG, "Profile card clicked → opening MobtiActivity");
        Intent i = new Intent(MypageActivity.this, com.moneybuddy.moneylog.mobti.activity.MobtiActivity.class);
        i.putExtra("code", String.valueOf(tvProfileMobtiLabel.getText()));
        startActivity(i);
    }
}
