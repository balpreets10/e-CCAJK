package com.mycca.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.mycca.R;
import com.mycca.listeners.DownloadCompleteListener;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.providers.CircleDataProvider;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.LocaleHelper;
import com.mycca.tools.Preferences;

public class SplashActivity extends AppCompatActivity {

    ImageView imageView;
    TextView tvSplashVersion;
    private Trace mTrace;
    private DataSnapshot dataSnap;
    int currentAppVersion;
    String currentVersionName;
    private String TAG = "Splash";
    Animation animationScale;

    private String LATEST_VERSION = "latest_version";
    private String WELCOME_MESSAGE = "welcome_message";

    private enum VersionCheckState {
        IDLE,
        STARTED,
        COMPLETE
    }

    private VersionCheckState versionCheckState = VersionCheckState.IDLE;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        bindViews();
        init();
        StartAnimations();
    }

    private void bindViews() {
        imageView = findViewById(R.id.logo);
        tvSplashVersion = findViewById(R.id.tv_splash_version);
    }

    private void init() {
         Helper.versionChecked = false;
        //FirebaseRemoteConfig.getInstance();

        currentAppVersion = Helper.getInstance().getAppVersion(this);
        if (currentAppVersion == -1)
            currentAppVersion = 6;

        currentVersionName = getAppVersionName();
        String text;
        if (currentVersionName.equals(""))
            text = getString(R.string.n_a);
        else
            text = currentVersionName;
        tvSplashVersion.setText(String.format(getString(R.string.version), text));

        CustomLogger.getInstance().logDebug(TAG + " " + currentAppVersion + ": " + currentVersionName, CustomLogger.Mask.SPLASH_ACTIVITY);
    }

    private void StartAnimations() {
        final Animation animationAlpha = AnimationUtils.loadAnimation(this, R.anim.alpha);
        animationScale = AnimationUtils.loadAnimation(this, R.anim.scale);
        final Animation animationBounce = AnimationUtils.loadAnimation(this, R.anim.bounce);

        animationAlpha.reset();
        animationScale.reset();
        animationBounce.reset();

        imageView.clearAnimation();
        imageView.startAnimation(animationScale);

        animationScale.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
                    @Override
                    public void OnConnectionAvailable() {
                        CustomLogger.getInstance().logDebug(TAG + " Connection Available", CustomLogger.Mask.SPLASH_ACTIVITY);
                        getInitialChecksData();
                    }

                    @Override
                    public void OnConnectionNotAvailable() {
                        CustomLogger.getInstance().logDebug(TAG + " Connection Not Available", CustomLogger.Mask.SPLASH_ACTIVITY);
                        if (Preferences.getInstance().getIntPref(SplashActivity.this, Preferences.PREF_CIRCLES) != -1)
                            CircleDataProvider.getInstance().setCircleData(false, getApplicationContext(), null);
                        else
                            CustomLogger.getInstance().logDebug("Circle data not available", CustomLogger.Mask.SPLASH_ACTIVITY);
                        updateState();
                    }
                });
                connectionUtility.checkConnectionAvailability();

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateState();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void updateState() {
        if (versionCheckState == VersionCheckState.IDLE)
            versionCheckState = VersionCheckState.STARTED;
        else if (versionCheckState == VersionCheckState.STARTED)
            versionCheckState = VersionCheckState.COMPLETE;
        CustomLogger.getInstance().logDebug("current state = " + versionCheckState);
        LoadNextActivity();
    }

    private void getInitialChecksData() {
        mTrace = FirebasePerformance.getInstance().newTrace("REQUEST_DATA_TRACE");
        mTrace.start();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataSnap = dataSnapshot;
                checkForNewVersion();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                updateState();
            }
        };
        FireBaseHelper.getInstance().getDataFromFireBase(null, valueEventListener, true, FireBaseHelper.ROOT_INITIAL_CHECKS);
    }

    private void checkForNewVersion() {
        mTrace.stop();
        mTrace = FirebasePerformance.getInstance().newTrace("VERSION_TRACE");
        mTrace.start();

        //        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
//                .build();
//        mFirebaseRemoteConfig.setConfigSettings(configSettings);
//        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
//
//        long cacheExpiration = 3600; // 1 hour in seconds.
//        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
//        // retrieve values from the service.
//        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
//            cacheExpiration = 0;
//        }
//
//        mFirebaseRemoteConfig.fetch(cacheExpiration)
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(SplashActivity.this, "Fetch Succeeded Latest Version = " + mFirebaseRemoteConfig.getString(LATEST_VERSION + "\nWelcome Message = " + WELCOME_MESSAGE),
//                                    Toast.LENGTH_SHORT).show();
//                            mTrace.stop();
//                            // After config data is successfully fetched, it must be activated before newly fetched
//                            // values are returned.
//                            mFirebaseRemoteConfig.activateFetched();
//
//                        } else {
//                            Toast.makeText(SplashActivity.this, "Fetch Failed",
//                                    Toast.LENGTH_SHORT).show();
//                            mTrace.stop();
//                        }
//                    }
//                });
        try {
            long value = (long) dataSnap.child(FireBaseHelper.ROOT_APP_VERSION).getValue();
            if (Helper.getInstance().onLatestVersion(value, SplashActivity.this))
                checkCircles();
        } catch (Exception e) {
            updateState();
        }
    }

    public void checkCircles() {
        mTrace.stop();
        mTrace = FirebasePerformance.getInstance().newTrace("CIRCLE_TRACE");
        mTrace.start();

        try {
            long circleCount = (long) dataSnap.child(FireBaseHelper.ROOT_CIRCLE_COUNT).getValue();
            if (circleCount == Preferences.getInstance().getIntPref(SplashActivity.this, Preferences.PREF_CIRCLES)) {
                CustomLogger.getInstance().logDebug("No new data", CustomLogger.Mask.SPLASH_ACTIVITY);
                checkActiveCircles();
            } else {
                CustomLogger.getInstance().logDebug("New data available", CustomLogger.Mask.SPLASH_ACTIVITY);
                CircleDataProvider.getInstance().setCircleData(true, getApplicationContext(), new DownloadCompleteListener() {
                    @Override
                    public void onDownloadSuccess() {
                        updateState();
                    }

                    @Override
                    public void onDownloadFailure() {
                        updateState();
                    }
                });

            }
        } catch (Exception e) {
            updateState();
        }
    }

    private void checkActiveCircles() {
        mTrace.stop();
        mTrace = FirebasePerformance.getInstance().newTrace("ACTIVE_CIRCLE_TRACE");
        mTrace.start();

        CustomLogger.getInstance().logDebug(TAG + " Checking Active Circles", CustomLogger.Mask.SPLASH_ACTIVITY);
        try {
            long activeCount = (long) dataSnap.child(FireBaseHelper.ROOT_ACTIVE_COUNT).getValue();
            if (activeCount == Preferences.getInstance().getIntPref(SplashActivity.this, Preferences.PREF_ACTIVE_CIRCLES)) {
                CustomLogger.getInstance().logDebug("No new data", CustomLogger.Mask.SPLASH_ACTIVITY);
                CircleDataProvider.getInstance().setCircleData(false, getApplicationContext(), null);
                updateState();
            } else {
                CustomLogger.getInstance().logDebug("New data available", CustomLogger.Mask.SPLASH_ACTIVITY);
                CircleDataProvider.getInstance().setCircleData(true, getApplicationContext(), new DownloadCompleteListener() {
                    @Override
                    public void onDownloadSuccess() {
                        updateState();
                    }

                    @Override
                    public void onDownloadFailure() {
                        updateState();
                    }
                });
            }

        } catch (Exception e) {
            updateState();
        }
    }

    private void LoadNextActivity() {
        if (versionCheckState == VersionCheckState.COMPLETE) {
            if (mTrace != null)
                mTrace.stop();
            Intent intent = new Intent();
            if (Preferences.getInstance().getCirclePref(this) == null) {
                intent.setClass(getApplicationContext(), StateSelectionActivity.class);
            } else if (Preferences.getInstance().getBooleanPref(this, Preferences.PREF_HELP_ONBOARDER)) {
                intent.setClass(getApplicationContext(), IntroActivity.class);
            } else {
                intent.setClass(getApplicationContext(), MainActivity.class);
            }
            startActivity(intent);
            finish();
        }
    }

    private String getAppVersionName() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}