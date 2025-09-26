package com.moneybuddy.moneylog.ledger.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.ledger.dto.request.LedgerCreateRequest;
import com.moneybuddy.moneylog.ledger.dto.response.LedgerEntryDto;
import com.moneybuddy.moneylog.ledger.repository.LedgerRepository;
import com.moneybuddy.moneylog.ledger.repository.ReceiptRepository;
import com.moneybuddy.moneylog.common.ResultCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LedgerWriteActivity extends AppCompatActivity {

    // ===== UI =====
    private ScrollView scrollRoot;
    private RadioGroup rgType;
    private RadioButton rbIncome, rbExpense;
    private TextView tvDate, tvTime;

    // 라벨
    private TextView labelAsset, labelCategory;

    // 입력
    private Spinner spinnerAsset, spinnerCategory;
    private TextView tvCategoryReadonly; // 수입 모드 노출
    private EditText editAmount, editMemo;

    // 기타 직접입력(있으면 사용)
    private EditText etCustomCategory;
    private EditText etCustomAsset;

    private ImageView btnBack, btnAnalyzeReceipt;
    private Button btnSave;

    // ===== Date/Time =====
    private final Calendar selectedDate = Calendar.getInstance();
    private final Calendar selectedTime = Calendar.getInstance();

    // ===== BG =====
    private Drawable bgNormal;
    private Drawable bgError;

    // ===== Edit mode =====
    private boolean isEditMode = false;
    private long editingId = -1L;

    // ===== Receipt pick/capture =====
    private Uri tempCameraUri;
    private ActivityResultLauncher<String> reqCamPermLauncher;
    private ActivityResultLauncher<String> reqReadImagesPermLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    // ===== Backend Repos =====
    private LedgerRepository ledgerRepo;
    private ReceiptRepository receiptRepo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger_write);

        // 1) 뷰 바인딩
        bindViews();

        // 2) 레포 초기화
        initRepositories();

        // 3) 나머지 초기화
        initAdapters();
        initDateTimePickers();
        initTypeSwitching();
        readIntentExtrasIfEdit(); // 편집 모드면 값 주입
        applyModeUI();            // UI 정리
        initReceiptLaunchers();   // 영수증 분석 런처/권한

        // 4) 버튼 리스너
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> trySave());
        btnAnalyzeReceipt.setOnClickListener(v -> showReceiptSourceDialog());
    }

    // ───────────────────── 바인딩 & 초기화 ─────────────────────
    private void bindViews() {
        scrollRoot = findViewById(R.id.scroll_root);
        rgType = findViewById(R.id.radio_group);
        rbIncome = findViewById(R.id.radio_income);
        rbExpense = findViewById(R.id.radio_expense);
        tvDate = findViewById(R.id.tv_date);
        tvTime = findViewById(R.id.tv_time);

        labelAsset = findViewById(R.id.label_asset);
        labelCategory = findViewById(R.id.label_category);

        spinnerAsset = findViewById(R.id.spinner_asset);
        spinnerCategory = findViewById(R.id.spinner_category);
        tvCategoryReadonly = findViewById(R.id.tv_category_readonly);

        editAmount = findViewById(R.id.edit_amount);
        editMemo = findViewById(R.id.edit_memo);

        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);
        btnAnalyzeReceipt = findViewById(R.id.btn_analyze_receipt);

        etCustomCategory = findOptionalEditTextByName("edit_custom_category", "tv_custom_category", "et_custom_category");
        etCustomAsset = findOptionalEditTextByName("edit_custom_asset", "tv_custom_asset", "et_custom_asset");

        bgNormal = ContextCompat.getDrawable(this, R.drawable.bg_input_box);
        bgError = ContextCompat.getDrawable(this, R.drawable.bg_input_box_error);
    }

    private void initRepositories() {
        ledgerRepo = new LedgerRepository(this, token());   // 시그니처에 맞춰 사용
        receiptRepo = new ReceiptRepository(this, token());
    }

    private void initAdapters() {
        ArrayAdapter<CharSequence> aAssets = ArrayAdapter.createFromResource(
                this, R.array.asset_list, android.R.layout.simple_spinner_item);
        aAssets.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAsset.setAdapter(aAssets);

        ArrayAdapter<CharSequence> aCategories = ArrayAdapter.createFromResource(
                this, R.array.category_list, android.R.layout.simple_spinner_item);
        aCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(aCategories);

        spinnerAsset.setOnItemSelectedListener(new SimpleItemSelectedListener(this::toggleCustomAssetIfNeeded));
        spinnerCategory.setOnItemSelectedListener(new SimpleItemSelectedListener(this::toggleCustomCategoryIfNeeded));
    }

    private void initDateTimePickers() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN);
        tvDate.setText(dateFormat.format(selectedDate.getTime()));
        tvDate.setOnClickListener(v -> {
            int y = selectedDate.get(Calendar.YEAR);
            int m = selectedDate.get(Calendar.MONTH);
            int d = selectedDate.get(Calendar.DAY_OF_MONTH);
            new DatePickerDialog(this, (view, yy, mm, dd) -> {
                selectedDate.set(yy, mm, dd);
                tvDate.setText(dateFormat.format(selectedDate.getTime()));
                setErrorBg(tvDate, false);
            }, y, m, d).show();
        });

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.KOREAN);
        tvTime.setText(timeFormat.format(selectedTime.getTime()));
        tvTime.setOnClickListener(v -> showTimeNumberPickerDialog());
    }

    private void initTypeSwitching() {
        rgType.setOnCheckedChangeListener((g, id) -> {
            markRadioError(false);
            applyModeUI();
        });

        editAmount.addTextChangedListener(new SimpleTextWatcher(() -> {
            editAmount.setError(null);
            setErrorBg(editAmount, false);
        }));
    }

    // ───────────────────── 수입/지출 모드 UI ─────────────────────
    private void applyModeUI() {
        final boolean income = isIncomeMode();

        // 자산: 수입이면 라벨+입력 숨김
        if (labelAsset != null) labelAsset.setVisibility(income ? View.GONE : View.VISIBLE);
        spinnerAsset.setVisibility(income ? View.GONE : View.VISIBLE);
        if (!income) {
            toggleCustomAssetIfNeeded();
        } else {
            if (etCustomAsset != null) etCustomAsset.setVisibility(View.GONE);
            setErrorBg(spinnerAsset, false);
        }

        // 카테고리
        if (income) {
            if (labelCategory != null) labelCategory.setVisibility(View.VISIBLE);
            spinnerCategory.setVisibility(View.GONE);
            if (etCustomCategory != null) etCustomCategory.setVisibility(View.GONE);

            if (tvCategoryReadonly != null) {
                tvCategoryReadonly.setText(getString(R.string.income)); // "수입"
                tvCategoryReadonly.setVisibility(View.VISIBLE);
            } else {
                // 읽기전용 뷰가 없을 때: 스피너를 비활성화+고정
                selectSpinnerItemByValue(spinnerCategory, getString(R.string.income));
                spinnerCategory.setEnabled(false);
                spinnerCategory.setVisibility(View.VISIBLE);
            }
        } else {
            if (labelCategory != null) labelCategory.setVisibility(View.VISIBLE);
            spinnerCategory.setEnabled(true);
            spinnerCategory.setVisibility(View.VISIBLE);
            if (tvCategoryReadonly != null) tvCategoryReadonly.setVisibility(View.GONE);
            toggleCustomCategoryIfNeeded();
        }
    }

    private boolean isIncomeMode() {
        return rgType.getCheckedRadioButtonId() == R.id.radio_income;
    }

    // ───────────────────── 편집 모드 인텐트 파싱 ─────────────────────
    private void readIntentExtrasIfEdit() {
        try {
            String mode = getIntent().getStringExtra("mode");
            if (!"edit".equals(mode)) return;

            isEditMode = true;
            editingId = getIntent().getLongExtra("id", -1L);

            String dateStr = getIntent().getStringExtra("date");
            if (!TextUtils.isEmpty(dateStr)) tvDate.setText(dateStr);

            String timeStr = getIntent().getStringExtra("time");
            if (!TextUtils.isEmpty(timeStr)) tvTime.setText(timeStr);

            if (getIntent().hasExtra("amount")) {
                long amt = getIntent().getLongExtra("amount", 0);
                editAmount.setText(String.valueOf(amt));
            } else {
                String amtStr = getIntent().getStringExtra("amount");
                if (!TextUtils.isEmpty(amtStr)) editAmount.setText(amtStr);
            }

            String memo = getIntent().getStringExtra("memo");
            if (TextUtils.isEmpty(memo)) memo = getIntent().getStringExtra("title");
            if (!TextUtils.isEmpty(memo)) editMemo.setText(memo);

            String typeName = getIntent().getStringExtra("type");
            if (!TextUtils.isEmpty(typeName)) {
                if ("INCOME".equalsIgnoreCase(typeName)) rbIncome.setChecked(true);
                else rbExpense.setChecked(true);
            }

            String asset = getIntent().getStringExtra("asset");
            if (!TextUtils.isEmpty(asset)) selectSpinnerItemByValue(spinnerAsset, asset);

            String category = getIntent().getStringExtra("category");
            if (!TextUtils.isEmpty(category)) {
                selectSpinnerItemByValue(spinnerCategory, category);
                if (tvCategoryReadonly != null && "INCOME".equalsIgnoreCase(typeName)) {
                    tvCategoryReadonly.setText(category);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ───────────────────── 시간 NumberPicker ─────────────────────
    private void showTimeNumberPickerDialog() {
        int initHour, initMinute;
        try {
            String[] hm = tvTime.getText().toString().trim().split(":");
            initHour = Integer.parseInt(hm[0]);
            initMinute = Integer.parseInt(hm[1]);
        } catch (Exception e) {
            Calendar now = Calendar.getInstance();
            initHour = now.get(Calendar.HOUR_OF_DAY);
            initMinute = now.get(Calendar.MINUTE);
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.HORIZONTAL);
        int pad = Math.round(getResources().getDisplayMetrics().density * 16);
        container.setPadding(pad, pad, pad, pad);

        LinearLayout colHour = new LinearLayout(this);
        colHour.setOrientation(LinearLayout.VERTICAL);
        colHour.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView labelHour = new TextView(this);
        labelHour.setText("시");
        labelHour.setPadding(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));

        final NumberPicker npHour = new NumberPicker(this);
        npHour.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npHour.setMinValue(0);
        npHour.setMaxValue(23);
        npHour.setValue(initHour);
        npHour.setWrapSelectorWheel(true);
        npHour.setFormatter(value -> String.format(Locale.KOREAN, "%02d", value));

        colHour.addView(labelHour);
        colHour.addView(npHour);

        LinearLayout colMinute = new LinearLayout(this);
        colMinute.setOrientation(LinearLayout.VERTICAL);
        colMinute.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView labelMinute = new TextView(this);
        labelMinute.setText("분");
        labelMinute.setPadding(0, 0, 0, (int) (8 * getResources().getDisplayMetrics().density));

        final NumberPicker npMinute = new NumberPicker(this);
        npMinute.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        npMinute.setMinValue(0);
        npMinute.setMaxValue(59);
        npMinute.setValue(initMinute);
        npMinute.setWrapSelectorWheel(true);
        npMinute.setFormatter(value -> String.format(Locale.KOREAN, "%02d", value));

        colMinute.addView(labelMinute);
        colMinute.addView(npMinute);

        container.addView(colHour);
        container.addView(colMinute);

        new AlertDialog.Builder(this)
                .setTitle("시간 선택 (24시간제)")
                .setView(container)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", (d, w) -> {
                    int h = npHour.getValue();
                    int m = npMinute.getValue();
                    selectedTime.set(Calendar.HOUR_OF_DAY, h);
                    selectedTime.set(Calendar.MINUTE, m);
                    tvTime.setText(String.format(Locale.KOREAN, "%02d:%02d", h, m));
                    setErrorBg(tvTime, false);
                })
                .show();
    }

    // ───────────────────── 저장 검증 & 백엔드 저장 ─────────────────────
    private void trySave() {
        List<String> missing = new ArrayList<>();
        View firstError = null;

        if (rgType.getCheckedRadioButtonId() == -1) {
            missing.add("유형(수입/지출)");
            markRadioError(true);
            if (firstError == null) firstError = rgType;
        }
        if (isEmpty(tvDate)) {
            missing.add("날짜");
            setErrorBg(tvDate, true);
            if (firstError == null) firstError = tvDate;
        }
        if (isEmpty(tvTime)) {
            missing.add("시간");
            setErrorBg(tvTime, true);
            if (firstError == null) firstError = tvTime;
        }

        String amountStr = editAmount.getText() != null ? editAmount.getText().toString().trim() : "";
        if (TextUtils.isEmpty(amountStr)) {
            missing.add("금액");
            editAmount.setError("금액을 입력해 주세요");
            setErrorBg(editAmount, true);
            if (firstError == null) firstError = editAmount;
        }

        if (!missing.isEmpty()) {
            Toast.makeText(this, "필수 항목이 빠졌습니다: " + TextUtils.join(", ", missing), Toast.LENGTH_SHORT).show();
            scrollTo(firstError);
            return;
        }

        // ==== DTO 구성 ====
        String type = isIncomeMode() ? "INCOME" : "EXPENSE";
        String date = tvDate.getText().toString();
        String time = tvTime.getText().toString();
        String dateTime = date + "T" + time + ":00"; // LocalDateTime 문자열

        String category = isIncomeMode() ? getString(R.string.income)
                : safeSelectedText(spinnerCategory, etCustomCategory);
        String asset = isIncomeMode() ? null : safeSelectedText(spinnerAsset, etCustomAsset);

        long amount = Long.parseLong(amountStr.replaceAll("[^0-9]", "")); // 항상 양수 전송
        String memo = editMemo.getText() != null ? editMemo.getText().toString().trim() : null;

        LedgerCreateRequest body = new LedgerCreateRequest();
        // LedgerCreateRequest가 세터를 제공한다면 세터 사용 권장:
        // body.setDateTime(dateTime); body.setEntryType(type); body.setAmount(amount) ...
        body.dateTime = dateTime;
        body.entryType = type;
        body.amount = amount;
        body.asset = asset;
        body.store = memo;         // 메모를 상호명으로도 사용 중이라면 그대로 둠
        body.category = category;
        body.description = memo;

        // 디버그 로그
        com.google.gson.Gson gson = new com.google.gson.Gson();
        Log.d("LedgerWrite", "REQ body=" + gson.toJson(body));

        // ==== 네트워크 호출: 성공 시에만 finish ====
        btnSave.setEnabled(false); // 중복 클릭 방지

        if (isEditMode && editingId > 0) {
            // 수정(Update)
            ledgerRepo.update(editingId, body, new ResultCallback<Void>() {
                @Override public void onSuccess(Void ignored) {
                    Toast.makeText(LedgerWriteActivity.this, "수정 완료", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
                @Override public void onError(Throwable t) {
                    btnSave.setEnabled(true);
                    Toast.makeText(LedgerWriteActivity.this, "수정 실패: " + (t.getMessage()==null ? "알 수 없는 오류" : t.getMessage()), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // 신규(Create)
            ledgerRepo.create(body, new ResultCallback<Long>() {
                @Override public void onSuccess(Long id) {
                    Toast.makeText(LedgerWriteActivity.this, "저장 완료", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
                @Override public void onError(Throwable t) {
                    btnSave.setEnabled(true);
                    Toast.makeText(LedgerWriteActivity.this, "저장 실패: " + (t.getMessage()==null ? "알 수 없는 오류" : t.getMessage()), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    // ───────────────────── 영수증 분석 (카메라/갤러리) ─────────────────────
    private void initReceiptLaunchers() {
        reqCamPermLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> { if (granted) launchCamera(); else toast("카메라 권한이 필요합니다."); });

        reqReadImagesPermLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> { if (granted) launchGallery(); else toast("사진 접근 권한이 필요합니다."); });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && tempCameraUri != null) onGotReceiptImage(tempCameraUri);
                    else toast("촬영이 취소되었습니다.");
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) onGotReceiptImage(uri); else toast("선택이 취소되었습니다."); });
    }

    private void showReceiptSourceDialog() {
        final String[] items = {"카메라로 촬영", "갤러리에서 선택"};
        new AlertDialog.Builder(this)
                .setTitle("영수증 분석")
                .setItems(items, (d, which) -> {
                    if (which == 0) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            reqCamPermLauncher.launch(Manifest.permission.CAMERA);
                        } else launchCamera();
                    } else {
                        String readPerm = Build.VERSION.SDK_INT >= 33
                                ? Manifest.permission.READ_MEDIA_IMAGES
                                : Manifest.permission.READ_EXTERNAL_STORAGE;
                        if (ContextCompat.checkSelfPermission(this, readPerm)
                                != PackageManager.PERMISSION_GRANTED) {
                            reqReadImagesPermLauncher.launch(readPerm);
                        } else launchGallery();
                    }
                })
                .show();
    }

    private void launchCamera() {
        tempCameraUri = createTempImageUri();
        if (tempCameraUri != null) takePictureLauncher.launch(tempCameraUri);
        else toast("임시 파일을 만들 수 없습니다.");
    }

    private void launchGallery() { pickImageLauncher.launch("image/*"); }

    private Uri createTempImageUri() {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (dir == null) dir = getCacheDir();
            File f = new File(dir, "receipt_" + System.currentTimeMillis() + ".jpg");
            return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", f);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void onGotReceiptImage(Uri uri) {
        analyzeReceipt(uri);
    }

    private void analyzeReceipt(Uri imageUri) {
        try {
            // 영수증 분석: 성공 시 onOcrResult로 연결
            receiptRepo.uploadOcr(imageUri, new ResultCallback<LedgerEntryDto>() {
                @Override public void onSuccess(LedgerEntryDto ocr) {
                    onOcrResult(ocr);
                }
                @Override public void onError(Throwable t) {
                    toast("영수증 분석 실패: " + (t.getMessage() == null ? "" : t.getMessage()));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            toast("분석 중 오류 발생");
        }
    }

    /** 외부(레포/옵저버)에서 OCR 결과를 넘겨줄 때 호출 */
    public void onOcrResult(LedgerEntryDto ocr) {
        if (ocr == null) { toast("영수증 분석 실패"); return; }

        // 타입
        if ("INCOME".equalsIgnoreCase(ocr.getEntryType())) rbIncome.setChecked(true);
        else rbExpense.setChecked(true);
        applyModeUI();

        // 날짜/시간
        String dt = ocr.getDateTime();
        if (dt != null && dt.length() >= 16) {
            String d = dt.substring(0, 10);
            String t = dt.substring(11, 16);
            tvDate.setText(d);
            tvTime.setText(t);
        }

        // 카테고리/자산
        if (isIncomeMode()) {
            if (tvCategoryReadonly != null && ocr.getCategory() != null) {
                tvCategoryReadonly.setText(ocr.getCategory());
            }
        } else {
            if (ocr.getCategory() != null) selectSpinnerItemByValue(spinnerCategory, ocr.getCategory());
            if (ocr.getAsset() != null) selectSpinnerItemByValue(spinnerAsset, ocr.getAsset());
        }

        // 금액(서버는 부호 적용)
        editAmount.setText(String.valueOf(Math.abs(ocr.getAmount())));

        // 메모/상호
        if (!TextUtils.isEmpty(ocr.getStore())) editMemo.setText(ocr.getStore());
        else if (!TextUtils.isEmpty(ocr.getDescription())) editMemo.setText(ocr.getDescription());

        toast("영수증 인식 완료");
    }

    // ───────────────────── 유틸 ─────────────────────
    private String token() {
        String t = com.moneybuddy.moneylog.common.TokenManager
                .getInstance(getApplicationContext())
                .getToken();

        if (t == null || t.isEmpty()) {
            SharedPreferences sp = getSharedPreferences("auth", MODE_PRIVATE);
            t = sp.getString("token", null);
            if (t == null) t = sp.getString("jwt", "");
        }
        return t == null ? "" : t;
    }

    private void selectSpinnerItemByValue(Spinner spinner, String value) {
        if (spinner == null || spinner.getAdapter() == null || TextUtils.isEmpty(value)) return;

        String target = value.trim();
        int count = spinner.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            Object item = spinner.getAdapter().getItem(i);
            String text = (item == null) ? "" : String.valueOf(item);
            if (target.equals(text)) {
                if (spinner.getSelectedItemPosition() != i) spinner.setSelection(i, false);
                return;
            }
        }
    }

    private void toggleCustomCategoryIfNeeded() {
        if (etCustomCategory == null) return;
        String sel = getSelectedText(spinnerCategory);
        boolean show = !isIncomeMode() && equalsAny(sel, getString(R.string.other), "기타");
        etCustomCategory.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) etCustomCategory.setText("");
    }

    private void toggleCustomAssetIfNeeded() {
        if (etCustomAsset == null) return;
        String sel = getSelectedText(spinnerAsset);
        boolean show = !isIncomeMode() && equalsAny(sel, getString(R.string.other), "기타");
        etCustomAsset.setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) etCustomAsset.setText("");
    }

    private String safeSelectedText(Spinner sp, EditText customIfOther) {
        String sel = getSelectedText(sp);
        if (equalsAny(sel, getString(R.string.other), "기타")) {
            String user = customIfOther != null ? customIfOther.getText().toString().trim() : "";
            return TextUtils.isEmpty(user) ? sel : user;
        }
        return sel;
    }

    private String getSelectedText(Spinner sp) {
        if (sp == null || sp.getSelectedItem() == null) return "";
        return String.valueOf(sp.getSelectedItem());
    }

    private boolean equalsAny(String s, String a, String b) {
        if (s == null) return false;
        return s.equals(a) || s.equals(b);
    }

    private EditText findOptionalEditTextByName(String... idNames) {
        for (String name : idNames) {
            int id = getResources().getIdentifier(name, "id", getPackageName());
            if (id != 0) {
                View v = findViewById(id);
                if (v instanceof EditText) return (EditText) v;
            }
        }
        return null;
    }

    private boolean isEmpty(TextView tv) {
        CharSequence s = tv.getText();
        return s == null || s.toString().trim().isEmpty();
    }

    private void setErrorBg(TextView v, boolean error) { v.setBackground(error ? bgError : bgNormal); }
    private void setErrorBg(EditText v, boolean error) { v.setBackground(error ? bgError : bgNormal); }
    private void setErrorBg(Spinner v, boolean error)  { v.setBackground(error ? bgError : bgNormal); }

    private void markRadioError(boolean on) {
        int color = on ? Color.parseColor("#C5463F") : Color.BLACK;
        rbIncome.setTextColor(color);
        rbExpense.setTextColor(color);
    }

    private void scrollTo(View target) {
        if (target == null || scrollRoot == null) return;
        scrollRoot.post(() -> scrollRoot.smoothScrollTo(0, Math.max(target.getTop() - 40, 0)));
        target.requestFocus();
    }

    private void toast(String msg) { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show(); }

    // ───────────────────── 내부 리스너/워처 ─────────────────────
    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        SimpleTextWatcher(Runnable onChange) { this.onChange = onChange; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { if (onChange != null) onChange.run(); }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }

    private static class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
        private final Runnable onSelect;
        SimpleItemSelectedListener(Runnable onSelect) { this.onSelect = onSelect; }
        @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { if (onSelect != null) onSelect.run(); }
        @Override public void onNothingSelected(AdapterView<?> parent) {}
    }
}
