package com.mycca.fragments;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.Helper;
import com.mycca.tools.FireBaseHelper;

public class FeedbackFragment extends Fragment {

    String TAG = "feedback";
    private Button btnRateApplication, btnSuggestion;
    private TextInputEditText etSuggestion;
    Activity activity;

    public FeedbackFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);
        setHasOptionsMenu(true);
        bindViews(view);
        init();
        return view;
    }

    private void bindViews(View view) {
        etSuggestion = view.findViewById(R.id.et_feedback_submit_suggestion);
        btnRateApplication = view.findViewById(R.id.btn_feedback_rate_application);
        btnSuggestion = view.findViewById(R.id.btn_feedback_submit_advice);
    }

    private void init() {

        activity = getActivity();
        btnSuggestion.setOnClickListener(v -> {
            if (!etSuggestion.getText().toString().trim().isEmpty()) {
                submitSuggestion();
            } else
                etSuggestion.setError(getString(R.string.no_suggestion));
        });

        btnRateApplication.setOnClickListener(v -> rateApplication());
    }

    private void rateApplication() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(Helper.getInstance().getPlayStoreURL())));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName())));
        }
    }

    private void submitSuggestion() {
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                CustomLogger.getInstance().logDebug("version checked= " + Helper.versionChecked, CustomLogger.Mask.FEEDBACK_FRAGMENT);
                if (Helper.versionChecked) {
                    submit();
                } else {
                    FireBaseHelper.getInstance().getDataFromFireBase(null, new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                long value = (long) dataSnapshot.getValue();
                                if (Helper.getInstance().onLatestVersion(value, getActivity()))
                                    submit();
                            } catch (Exception e) {
                                Helper.getInstance().showMaintenanceDialog(getActivity(), null);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Helper.getInstance().showMaintenanceDialog(activity, null);
                        }
                    }, true, FireBaseHelper.ROOT_INITIAL_CHECKS,FireBaseHelper.ROOT_APP_VERSION);
                }

            }

            @Override
            public void OnConnectionNotAvailable() {
                Helper.getInstance().noInternetDialog(activity);
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    private void submit() {
        Task<Void> task = FireBaseHelper.getInstance().pushOnFireBase(etSuggestion.getText().toString().trim(),
                FireBaseHelper.ROOT_SUGGESTIONS);

        task.addOnCompleteListener((Task<Void> task1) -> {

            if (task1.isSuccessful()) {
                Helper.getInstance().showMessage(getActivity(), "",
                        getString(R.string.thanks_for_feedback),
                        FancyAlertDialogType.SUCCESS);
            } else {
                if (FireBaseHelper.getInstance().getAuth().getCurrentUser() == null) {
                    Helper.getInstance().showFancyAlertDialog(activity, getString(R.string.suggestion_sign_in),
                            getString(R.string.sign_in_with_google),
                            getString(R.string.sign_in),
                            () -> ((MainActivity) activity).signInWithGoogle(),
                            getString(R.string.cancel),
                            () -> {

                            },
                            FancyAlertDialogType.ERROR);
                } else {
                    Helper.getInstance().showMaintenanceDialog(activity, null);
                }

            }
        });
    }
}
