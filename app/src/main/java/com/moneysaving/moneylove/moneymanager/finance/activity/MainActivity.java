package com.moneysaving.moneylove.moneymanager.finance.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.Utils.TransactionUpdateEvent;
import com.moneysaving.moneylove.moneymanager.finance.fragment.BudgetFragment;
import com.moneysaving.moneylove.moneymanager.finance.fragment.HomeFragment;
import com.moneysaving.moneylove.moneymanager.finance.fragment.SettingsFragment;
import com.moneysaving.moneylove.moneymanager.finance.fragment.StatisticsFragment;
import com.moneysaving.moneylove.moneymanager.finance.model.TransactionModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_TRANSACTION_REQUEST = 1;
    private LinearLayout navHome, navStatistic, navBudget, navSettings;
    private ImageView fabAdd;
    private ImageView ivHome, ivStatistic, ivBudget, ivSettings;
    private TextView tvHome, tvStatistic, tvBudget, tvSettings;

    private Fragment activeFragment;
    private SharePreferenceUtils sharePreferenceUtils;
    private List<TransactionModel> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharePreferenceUtils = new SharePreferenceUtils(this);
        sharePreferenceUtils.incrementCounter();


        sharePreferenceUtils = new SharePreferenceUtils(this);
        transactionList = sharePreferenceUtils.getTransactionList();

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

        if (fragment instanceof HomeFragment) {
            Bundle bundle = new Bundle();
            bundle.putString("transactionList", new Gson().toJson(transactionList));
            fragment.setArguments(bundle);
        } else if (fragment instanceof StatisticsFragment) {
            Bundle bundle = new Bundle();
            bundle.putString("transactionList", new Gson().toJson(transactionList));
            fragment.setArguments(bundle);
        } else if (fragment instanceof BudgetFragment) {
            Bundle bundle = new Bundle();
            bundle.putString("transactionList", new Gson().toJson(transactionList));
            fragment.setArguments(bundle);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_TRANSACTION_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("transactionData")) {
                String transactionJson = data.getStringExtra("transactionData");
                TransactionModel newTransaction = TransactionModel.fromJson(transactionJson);

                if (newTransaction != null) {
                    transactionList = sharePreferenceUtils.getTransactionList();

                    // Tải lại fragment hiện tại để cập nhật dữ liệu
                    if (activeFragment instanceof HomeFragment) {
                        loadFragment(new HomeFragment());
                    } else if (activeFragment instanceof StatisticsFragment) {
                        loadFragment(new StatisticsFragment());
                    } else if (activeFragment instanceof BudgetFragment) {
                        loadFragment(new BudgetFragment());
                    }

                    EventBus.getDefault().post(new TransactionUpdateEvent(transactionList));

                    Toast.makeText(this, "Đã thêm giao dịch mới", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public List<TransactionModel> getTransactionList() {
        return transactionList;
    }
}