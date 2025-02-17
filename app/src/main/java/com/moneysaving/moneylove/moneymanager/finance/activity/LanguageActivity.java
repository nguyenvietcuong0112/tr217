package com.moneysaving.moneylove.moneymanager.finance.activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.mallegan.ads.callback.NativeCallback;
import com.mallegan.ads.util.Admob;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.Constant;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SystemConfiguration;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SystemUtil;
import com.moneysaving.moneylove.moneymanager.finance.adapter.LanguageStartAdapter;
import com.moneysaving.moneylove.moneymanager.finance.base.BaseActivity;
import com.moneysaving.moneylove.moneymanager.finance.databinding.ActivityLanguageBinding;


import java.util.Map;


public class LanguageActivity extends BaseActivity {

    String codeLang = "";
    LanguageStartAdapter languageAdapter;
    ActivityLanguageBinding binding;

    private SharePreferenceUtils sharePreferenceUtils;

    boolean isNativeLanguageSelectLoaded = false;

    @Override
    public void bind() {
        SystemConfiguration.setStatusBarColor(this, R.color.white, SystemConfiguration.IconColor.ICON_DARK);
        SystemUtil.setLocale(this);
        binding = ActivityLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        boolean fromSettings = getIntent().getBooleanExtra("from_settings", false);



        if (SharePreferenceUtils.isOrganic(this)) {
            AppsFlyerLib.getInstance().registerConversionListener(this, new AppsFlyerConversionListener() {

                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    String mediaSource = (String) conversionData.get("media_source");
                    SharePreferenceUtils.setOrganicValue(getApplicationContext(), mediaSource == null || mediaSource.isEmpty() || mediaSource.equals("organic"));
                }

                @Override
                public void onConversionDataFail(String s) {
                    // Handle conversion data failure
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> map) {
                    // Handle app open attribution
                }

                @Override
                public void onAttributionFailure(String s) {
                    // Handle attribution failure
                }
            });
        }
        languageAdapter = new LanguageStartAdapter(this, Constant.getLanguage(), data -> {
            codeLang = data.getIsoLanguage();
            binding.ivSelect.setVisibility(View.VISIBLE);
            binding.ivSelect.setAlpha(1.0f);
            binding.ivSelect.setSelected(true);
//            SharedPreferences sharedPreferences = getSharedPreferences("language_select", MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            boolean isFirstLoad = sharedPreferences.getBoolean("isFirstLoadAds", true);
//            if (isFirstLoad) {
//                loadAdsNativeLanguageSelect();
//                editor.putBoolean("isFirstLoadAds", false);
//                editor.apply();
//            }

            if (!isNativeLanguageSelectLoaded) {
                loadAdsNativeLanguageSelect();
            }
        });
        if (fromSettings) {
//            binding.ivBack.setVisibility(View.VISIBLE);
            binding.frAds.setVisibility(View.GONE);

        }
        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.rvLanguage.setAdapter(languageAdapter);
        binding.ivSelect.setOnClickListener(v -> {
            if (codeLang != "") {
                SystemUtil.saveLocale(this, codeLang);
                if (fromSettings) {
                    finish();
                } else {
//                    sharePreferenceUtils = new SharePreferenceUtils(this);
//                    int counterValue = sharePreferenceUtils.getCurrentValue();
//                    if (counterValue == 0) {
//                        startActivity(new Intent(LanguageActivity.this, GuideActivity.class));
//                    } else {
                        startActivity(new Intent(LanguageActivity.this, IntroActivity.class));
//                    }
                }
            } else {
                Toast.makeText(this, "Please choose a language to continue", Toast.LENGTH_LONG).show();

            }
        });
        binding.ivSelect.setVisibility(View.GONE);
        loadAdsNative();
    }

    public void loadAdsNativeLanguageSelect() {
        NativeAdView adView;
        if (SharePreferenceUtils.isOrganic(this)) {
            adView = (NativeAdView) LayoutInflater.from(this)
                    .inflate(R.layout.layout_native_language, null);
        } else {
            adView = (NativeAdView) LayoutInflater.from(this)
                    .inflate(R.layout.layout_native_language_non_organic, null);
        }
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(LanguageActivity.this, getString(R.string.native_language_select), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                isNativeLanguageSelectLoaded = true;
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                checkNextButtonStatus(true);
            }

            @Override
            public void onAdFailedToLoad() {
                binding.frAds.removeAllViews();
                checkNextButtonStatus(true);
            }
        });
    }


    private void loadAdsNative() {
        checkNextButtonStatus(false);
        Admob.getInstance().loadNativeAd(LanguageActivity.this, getString(R.string.native_language), new NativeCallback() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                super.onNativeAdLoaded(nativeAd);
                NativeAdView adView = new NativeAdView(LanguageActivity.this);
                if (!SharePreferenceUtils.isOrganic(LanguageActivity.this)) {
                    adView = (NativeAdView) LayoutInflater.from(LanguageActivity.this).inflate(R.layout.layout_native_language_non_organic, null);
                } else {
                    adView = (NativeAdView) LayoutInflater.from(LanguageActivity.this).inflate(R.layout.layout_native_language, null);
                }
                binding.frAds.removeAllViews();
                binding.frAds.addView(adView);
                Admob.getInstance().pushAdsToViewCustom(nativeAd, adView);
                checkNextButtonStatus(true);
            }

            @Override
            public void onAdFailedToLoad() {
                super.onAdFailedToLoad();
                binding.frAds.removeAllViews();
                checkNextButtonStatus(true);
            }

        });
    }

    private void checkNextButtonStatus(boolean isReady) {
        if (isReady) {
            binding.ivSelect.setVisibility(View.VISIBLE);
            binding.btnNextLoading.setVisibility(View.GONE);
        } else {
            binding.ivSelect.setVisibility(View.GONE);
            binding.btnNextLoading.setVisibility(View.VISIBLE);
        }
    }


}