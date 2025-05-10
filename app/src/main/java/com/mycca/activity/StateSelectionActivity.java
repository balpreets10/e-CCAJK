package com.mycca.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.mycca.R;
import com.mycca.adapter.GenericSpinnerAdapter;
import com.mycca.models.Circle;
import com.mycca.providers.CircleDataProvider;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.Preferences;

public class StateSelectionActivity extends AppCompatActivity {

    Spinner circleSpinner;
    RadioGroup languageRadioGroup;
    Button proceed, close;
    TextView noData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_selection);
        bindViews();
        init();
    }

    private void bindViews() {
        circleSpinner = findViewById(R.id.spinner_circle);
        languageRadioGroup = findViewById(R.id.radio_group_lang);
        proceed = findViewById(R.id.button_proceed);
        close = findViewById(R.id.button_close);
        noData = findViewById(R.id.tv_no_data);
    }

    private void init() {

        if (CircleDataProvider.getInstance().getActiveCircleData() == null) {
            noData.setVisibility(View.VISIBLE);
            close.setVisibility(View.VISIBLE);
            close.setOnClickListener(v -> finish());

        } else {
            GenericSpinnerAdapter<Circle> adapter = new GenericSpinnerAdapter<>(this, CircleDataProvider.getInstance().getActiveCircleData());
            circleSpinner.setAdapter(adapter);

            proceed.setOnClickListener(v -> {
                if (languageRadioGroup.getCheckedRadioButtonId() == R.id.radioEnglish)
                    Preferences.getInstance().setStringPref(this, Preferences.PREF_LANGUAGE, "en");
                if (languageRadioGroup.getCheckedRadioButtonId() == R.id.radioHindi)
                    Preferences.getInstance().setStringPref(this, Preferences.PREF_LANGUAGE, "hi");
                Preferences.getInstance().setModelPref(this, Preferences.PREF_CIRCLE_DATA, circleSpinner.getSelectedItem());

                //LocaleHelper.setLocale(getApplicationContext());
                Helper.resetInstance();
                FireBaseHelper.getInstance().getOtherStateData(this, null);

                startActivity(new Intent(StateSelectionActivity.this, IntroActivity.class));
                finish();
            });
        }
    }
}
