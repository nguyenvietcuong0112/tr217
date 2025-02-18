package com.moneysaving.moneylove.moneymanager.finance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.fragment.BudgetFragment;
import com.moneysaving.moneylove.moneymanager.finance.fragment.HomeFragment;
import com.moneysaving.moneylove.moneymanager.finance.fragment.SettingsFragment;
import com.moneysaving.moneylove.moneymanager.finance.fragment.StatisticsFragment;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_TRANSACTION_REQUEST = 1;
    private LinearLayout navHome, navStatistic, navBudget, navSettings;
    private ImageView fabAdd;
    private ImageView ivHome, ivStatistic, ivBudget, ivSettings;
    private TextView tvHome, tvStatistic, tvBudget, tvSettings;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharePreferenceUtils sharePreferenceUtils = new SharePreferenceUtils(this);
        TransactionModel transaction = sharePreferenceUtils.getTransaction();

        if (transaction != null) {
            String transactionType = transaction.getTransactionType();
            String amount = transaction.getAmount();
            String budget = transaction.getBudget();
            String category = transaction.getCategory();
            String note = transaction.getNote();
            String date = transaction.getDate();
            String time = transaction.getTime();

        }

        initializeViews();
        setupClickListeners();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            updateIcons(0);
        }
    }

    private void initializeViews() {
        navHome = findViewById(R.id.nav_home);
        navStatistic = findViewById(R.id.nav_statistic);
        fabAdd = findViewById(R.id.fab_add);           
        navBudget = findViewById(R.id.nav_budget);
        navSettings = findViewById(R.id.nav_settings);

        ivHome = findViewById(R.id.iv_home);           
        ivStatistic = findViewById(R.id.iv_statistic); 
        ivBudget = findViewById(R.id.iv_budget);       
        ivSettings = findViewById(R.id.iv_settings);

        tvHome = findViewById(R.id.tv_home);
        tvStatistic = findViewById(R.id.tv_statistic);
        tvBudget = findViewById(R.id.tv_budget);
        tvSettings = findViewById(R.id.tv_settings);
    }

    private void setupClickListeners() {
        navHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            updateIcons(0);
        });

        navStatistic.setOnClickListener(v -> {
            loadFragment(new StatisticsFragment());
            updateIcons(1);
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivityForResult(intent, ADD_TRANSACTION_REQUEST);
        });

        navBudget.setOnClickListener(v -> {
            loadFragment(new BudgetFragment());
            updateIcons(2);
        });

        navSettings.setOnClickListener(v -> {
            loadFragment(new SettingsFragment());
            updateIcons(3);
        });
    }

    private void loadFragment(Fragment fragment) {
        if (activeFragment != null && activeFragment.getClass() == fragment.getClass()) {
            return;
        }

        activeFragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
    private void updateIcons(int selectedIndex) {
        ivHome.setColorFilter(getResources().getColor(R.color.icon_inactive));
        ivStatistic.setColorFilter(getResources().getColor(R.color.icon_inactive));
        ivBudget.setColorFilter(getResources().getColor(R.color.icon_inactive));
        ivSettings.setColorFilter(getResources().getColor(R.color.icon_inactive));

        tvHome.setTextColor(getResources().getColor(R.color.icon_inactive));
        tvStatistic.setTextColor(getResources().getColor(R.color.icon_inactive));
        tvBudget.setTextColor(getResources().getColor(R.color.icon_inactive));
        tvSettings.setTextColor(getResources().getColor(R.color.icon_inactive));

        switch (selectedIndex) {
            case 0:
                ivHome.setColorFilter(getResources().getColor(R.color.blue));
                tvHome.setTextColor(getResources().getColor(R.color.blue));
                break;
            case 1:
                ivStatistic.setColorFilter(getResources().getColor(R.color.blue));
                tvStatistic.setTextColor(getResources().getColor(R.color.blue));
                break;
            case 2:
                ivBudget.setColorFilter(getResources().getColor(R.color.blue));
                tvBudget.setTextColor(getResources().getColor(R.color.blue));
                break;
            case 3:
                ivSettings.setColorFilter(getResources().getColor(R.color.blue));
                tvSettings.setTextColor(getResources().getColor(R.color.blue));
                break;
        }
    }
}