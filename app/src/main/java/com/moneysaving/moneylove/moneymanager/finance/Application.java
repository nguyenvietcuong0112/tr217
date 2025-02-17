//package com.time.warp.timewarp.scan.face.scanner.CommonApp;
//
//import android.content.Context;
//import android.os.Build;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//
//import com.appsflyer.AppsFlyerLib;
//import com.appsflyer.adrevenue.AppsFlyerAdRevenue;
//import com.facebook.FacebookSdk;
//import com.mallegan.ads.util.AdsApplication;
//import com.mallegan.ads.util.AppOpenManager;
//import com.time.warp.timewarp.scan.face.scanner.R;
//import com.time.warp.timewarp.scan.face.scanner.utils.SharePreferenceUtils;
//
//
//import java.io.File;
//import java.util.List;
//
//public class Application extends AdsApplication {
//    private static Application instance;
//
//    //    public static FirebaseAnalytics mFirebaseAnalytics;
//    @Override
//    public boolean enableAdsResume() {
//        return true;
//    }
//
//    @Override
//    public List<String> getListTestDeviceId() {
//        return null;
//    }
//
//    @Override
//    public String getResumeAdId() {
//        return getString(R.string.open_resume);
//    }
//
//    @Override
//    public Boolean buildDebug() {
//        return null;
//    }
//
//    public void onCreate() {
//        super.onCreate();
//        instance = this;
//        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
//        AppsFlyerAdRevenue.Builder afRevenueBuilder = new AppsFlyerAdRevenue.Builder(this);
//        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));
//        AppsFlyerAdRevenue.initialize(afRevenueBuilder.build());
//        AppsFlyerLib.getInstance().init(this.getString(R.string.AF_DEV_KEY), null, this);
//        AppsFlyerLib.getInstance().start(this);
//
//        if (!SharePreferenceUtils.isFullAds(this)) {
//            SharePreferenceUtils.setFullAds(this, isEmulator(this));
//        }
//    }
//
//    public static boolean isEmulator1(Context context) {
//        return (Build.FINGERPRINT.startsWith("generic") ||
//                Build.FINGERPRINT.contains("generic") ||
//                Build.FINGERPRINT.contains("unknown") ||
//                Build.MODEL.contains("google_sdk") ||
//                Build.MODEL.contains("Emulator") ||
//                Build.MODEL.contains("Android SDK built for x86") ||
//                Build.MANUFACTURER.contains("Genymotion") ||
//                Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") ||
//                "google_sdk".equals(Build.PRODUCT) ||
//                !hasTelephony(context) ||
//                checkForEmulatorFiles());
//    }
//
//    public static boolean checkForEmulatorFiles() {
//        String[] knownEmulatorFiles = {
//                "/dev/socket/qemud",
//                "/dev/qemu_pipe",
//                "/system/lib/libc_malloc_debug_qemu.so",
//                "/sys/qemu_trace",
//                "/system/bin/qemu-props"
//        };
//
//        for (String file : knownEmulatorFiles) {
//            File f = new File(file);
//            if (f.exists()) {
//                return true;  // Phát hiện file giả lập
//            }
//        }
//        return false;
//    }
//
//    public static boolean isEmulator(Context context) {
//        boolean result = isEmulator1(context);  //
//        if (!hasTelephony(context)) {
//            result = true;  // Nếu không có telephony (SIM), khả năng cao là giả lập
//        }
//        return result;
//
//    }
//
//    public static boolean hasTelephony(Context context) {
//        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
//    }
//
//}


package com.moneysaving.moneylove.moneymanager.finance;

import com.appsflyer.AppsFlyerConversionListener;
import com.facebook.FacebookSdk;
import com.google.firebase.FirebaseApp;
import com.mallegan.ads.util.AdsApplication;
import com.mallegan.ads.util.AppOpenManager;
import com.mallegan.ads.util.AppsFlyer;
import com.moneysaving.moneylove.moneymanager.finance.R;
import com.moneysaving.moneylove.moneymanager.finance.Utils.SharePreferenceUtils;
import com.moneysaving.moneylove.moneymanager.finance.activity.IntroActivity;
import com.moneysaving.moneylove.moneymanager.finance.activity.LanguageActivity;
import com.moneysaving.moneylove.moneymanager.finance.activity.SplashActivity;


import java.util.List;
import java.util.Map;

public class Application extends AdsApplication {

    @Override
    public boolean enableAdsResume() {
        return true;
    }

    @Override
    public List<String> getListTestDeviceId() {
        return null;
    }

    @Override
    public String getResumeAdId() {
        return getString(R.string.open_resume);
    }

    @Override
    public Boolean buildDebug() {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(LanguageActivity.class);
        AppOpenManager.getInstance().disableAppResumeWithActivity(IntroActivity.class);
        FacebookSdk.setClientToken(getString(R.string.facebook_client_token));

        if (!SharePreferenceUtils.isOrganic(getApplicationContext())) {
            AppsFlyer.getInstance().initAppFlyer(this, getString(R.string.AF_DEV_KEY), true);

        } else {
            AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
                @Override
                public void onConversionDataSuccess(Map<String, Object> conversionData) {
                    String mediaSource = (String) conversionData.get("media_source");

                    SharePreferenceUtils.setOrganicValue(getApplicationContext(), mediaSource == null || mediaSource.isEmpty() || mediaSource.equals("organic"));
                }

                @Override
                public void onConversionDataFail(String errorMessage) {
                    // Handle conversion data failure
                }

                @Override
                public void onAppOpenAttribution(Map<String, String> attributionData) {
                    // Handle app open attribution
                }

                @Override
                public void onAttributionFailure(String errorMessage) {
                    // Handle attribution failure
                }
            };

            AppsFlyer.getInstance().initAppFlyer(this, getString(R.string.AF_DEV_KEY), true, conversionListener);

        }
    }


}