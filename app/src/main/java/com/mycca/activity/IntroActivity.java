package com.mycca.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mycca.R;
import com.mycca.custom.Onboarder.OnboarderActivity;
import com.mycca.custom.Onboarder.OnboarderPage;
import com.mycca.tools.LocaleHelper;
import com.mycca.tools.Preferences;

import java.util.ArrayList;
import java.util.List;


public class IntroActivity extends OnboarderActivity {

    boolean fromSettings = false;
    List<OnboarderPage> onBoarderPages;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
      }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fromSettings = getIntent().getBooleanExtra("FromSettings", false);
        onBoarderPages = new ArrayList<>();

        try {
            OnboarderPage onboarderPage1 = new OnboarderPage(getString(R.string.tutorial_title0),
                    getString(R.string.tutorial_text0),
                    R.drawable.cca2);
            setOnboarderPageProperties(onboarderPage1);

            OnboarderPage onboarderPage2 = new OnboarderPage(getString(R.string.tutorial_title1),
                    getString(R.string.tutorial_text1),
                    R.drawable.drawable_functions);
            setOnboarderPageProperties(onboarderPage2);

            OnboarderPage onboarderPage3 = new OnboarderPage(getString(R.string.tutorial_title2),
                    getString(R.string.tutorial_text2),
                    R.drawable.drawable_track);
            setOnboarderPageProperties(onboarderPage3);

            OnboarderPage onboarderPage4 = new OnboarderPage(getString(R.string.tutorial_title3),
                    getString(R.string.tutorial_text3),
                    R.drawable.drawable_update_info);
            setOnboarderPageProperties(onboarderPage4);

            OnboarderPage onboarderPage5 = new OnboarderPage(getString(R.string.tutorial_title4),
                    getString(R.string.tutorial_text4),
                    R.drawable.index);
            setOnboarderPageProperties(onboarderPage5);

            onBoarderPages.add(onboarderPage1);
            onBoarderPages.add(onboarderPage2);
            onBoarderPages.add(onboarderPage3);
            onBoarderPages.add(onboarderPage4);
            onBoarderPages.add(onboarderPage5);

            setOnboardPagesReady(onBoarderPages);

        } catch (Exception e) {
            e.printStackTrace();
            nextActions();
        }
    }

    public void setOnboarderPageProperties(OnboarderPage onboarderPage) {
        onboarderPage.setTitleColor(R.color.colorPrimary);
        onboarderPage.setDescriptionColor(R.color.colorOffWhite);
        onboarderPage.setBackgroundColor(R.color.colorLightBlack);
        onboarderPage.setDescriptionTextSize(20);
        onboarderPage.setTitleTextSize(25);
        onboarderPage.setMultilineDescriptionCentered(true);
    }

    public void nextActions() {
        Intent intent = new Intent(IntroActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (!fromSettings)
            intent.putExtra("First", true);
        startActivity(intent);
        finish();
    }

    @Override
    public void onSkipButtonPressed() {
        Preferences.getInstance().setBooleanPref(this, Preferences.PREF_HELP_ONBOARDER, false);
        nextActions();
    }

    @Override
    public void onFinishButtonPressed() {
        Preferences.getInstance().setBooleanPref(this, Preferences.PREF_HELP_ONBOARDER, false);
        nextActions();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fromSettings)
            Preferences.getInstance().setBooleanPref(this, Preferences.PREF_HELP_ONBOARDER, false);
    }
}
