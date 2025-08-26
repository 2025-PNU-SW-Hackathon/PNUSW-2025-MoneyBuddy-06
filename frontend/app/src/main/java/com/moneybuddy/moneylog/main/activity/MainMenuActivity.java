package com.moneybuddy.moneylog.main.activity;

import android.os.Bundle;
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
import com.moneybuddy.moneylog.main.fragment.MainMenuMypageFragment;

public class MainMenuActivity extends AppCompatActivity {

    private FragmentManager fragmentManager = getSupportFragmentManager();
    private MainMenuHomeFragment fragmentHome = new MainMenuHomeFragment();
    private MainMenuChallengeFragment fragmentChallenge = new MainMenuChallengeFragment();
    private MainMenuLedgerFragment fragmentLedger = new MainMenuLedgerFragment();
    private MainMenuMypageFragment fragmentMypage = new MainMenuMypageFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.menu_frame_layout, fragmentHome).commitAllowingStateLoss();

        BottomNavigationView bottomNavigationView = findViewById(R.id.menu_bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new ItemSelectedListener());
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