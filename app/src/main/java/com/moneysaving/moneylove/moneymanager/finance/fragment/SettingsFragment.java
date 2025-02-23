package com.moneysaving.moneylove.moneymanager.finance.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.mallegan.ads.util.AppOpenManager;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.activity.CurrencyUnitActivity;
import com.moneysaving.moneylove.moneymanager.finance.activity.LanguageActivity;
import com.moneysaving.moneylove.moneymanager.finance.activity.MainActivity;
import com.moneysaving.moneylove.moneymanager.finance.adapter.TransactionAdapter;

import java.util.Timer;
import java.util.TimerTask;

public class SettingsFragment extends Fragment {
    private boolean isBtnProcessing = false;
    private static final int REQUEST_CURRENCY_SELECT = 100;
    String currentCurrency;

    TextView tvCurrency;

    LinearLayout btnShare,btnLanguage,btnRateUs,btnPrivacyPolicy,llCurrency;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        setupClickListeners();


        return view;
    }

    private void initViews(View view) {
        currentCurrency = SharePreferenceUtils.getSelectedCurrencyCode(getContext());
        if (currentCurrency.isEmpty()) currentCurrency = "$";

        btnShare = view.findViewById(R.id.btn_share);
        btnLanguage = view.findViewById(R.id.btn_language);
        btnPrivacyPolicy = view.findViewById(R.id.btn_privacy_policy);
        btnRateUs = view.findViewById(R.id.btn_rate_us);
        llCurrency = view.findViewById(R.id.llCurrency);

        tvCurrency = view.findViewById(R.id.tv_currency);
        tvCurrency.setText(currentCurrency);
    }

    private void setupClickListeners() {
        btnShare.setOnClickListener(v -> {
            if (isBtnProcessing) return;
            isBtnProcessing = true;

            Intent myIntent = new Intent(Intent.ACTION_SEND);
            myIntent.setType("text/plain");
            String body = "có link app thì điền vào";
            String sub = "AI Money";
            myIntent.putExtra(Intent.EXTRA_SUBJECT, sub);
            myIntent.putExtra(Intent.EXTRA_TEXT, body);
            startActivity(Intent.createChooser(myIntent, "Share"));
            AppOpenManager.getInstance().disableAppResumeWithActivity(MainActivity.class);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isBtnProcessing = false;
                }
            }, 1000);
        });

        btnLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), LanguageActivity.class);
            intent.putExtra("from_settings", true);
            startActivity(intent);
        });

        btnRateUs.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                this.startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                this.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=")));
            }
        });


        btnPrivacyPolicy.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://antistudio.netlify.app/policy");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });


        llCurrency.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CurrencyUnitActivity.class);
            intent.putExtra(CurrencyUnitActivity.EXTRA_FROM_SETTINGS, true);
            startActivityForResult(intent, REQUEST_CURRENCY_SELECT);
        });


    }

}