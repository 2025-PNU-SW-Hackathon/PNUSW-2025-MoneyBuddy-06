package com.moneybuddy.moneylog.main.activity;

import android.os.Bundle;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.moneybuddy.moneylog.R;
import com.moneybuddy.moneylog.main.fragment.MainMenuChallengeFragment;
import com.moneybuddy.moneylog.main.fragment.MainMenuHomeFragment;
import com.moneybuddy.moneylog.main.fragment.MainMenuLedgerFragment;

@ExperimentalBadgeUtils
public class MainMenuActivity extends AppCompatActivity {

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private MainMenuHomeFragment fragmentHome = new MainMenuHomeFragment();
    private MainMenuChallengeFragment fragmentChallenge = new MainMenuChallengeFragment();
    private MainMenuLedgerFragment fragmentLedger = new MainMenuLedgerFragment();
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();

        bottomNavigationView = findViewById(R.id.menu_bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.menu_home);
        bottomNavigationView.setOnItemSelectedListener(new ItemSelectedListener());
    }

    public void navigateToTab(int menuId) {
        // BottomNavigationView의 아이템을 강제로 선택합니다.
        // 이렇게 하면 ItemSelectedListener가 자동으로 호출되어,
        // 아이콘 변경과 프래그먼트 교체가 동시에 일어납니다.
        bottomNavigationView.setSelectedItemId(menuId);
    }

    class ItemSelectedListener implements BottomNavigationView.OnItemSelectedListener {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            int id = menuItem.getItemId();
            if (id == R.id.menu_home) {
                transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();
            } else if (id == R.id.menu_challenge) {
                transaction.replace(R.id.menu_frame_layout, fragmentChallenge).commitAllowingStateLoss();
            } else if (id == R.id.menu_ledger) {
                transaction.replace(R.id.menu_frame_layout, fragmentLedger).commitAllowingStateLoss();
            }
            return true;
        }
    }
}