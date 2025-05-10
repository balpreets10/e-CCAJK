package com.mycca.fragments;


import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mycca.R;
import com.mycca.activity.IntroActivity;
import com.mycca.activity.MainActivity;
import com.mycca.activity.StateSettingActivity;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.models.StaffModel;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.LocaleHelper;
import com.mycca.tools.Preferences;

import java.util.HashMap;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    FirebaseAuth mAuth;
    private Switch switchNotification;
    ScrollView parentLayout;
    LinearLayout layoutChangeState, layoutSignInOut, layoutChangePwd, layoutChangeLang;
    private TextView tvCurrentState;
    private TextView tvSignOut;
    private TextView tvAccount;
    private TextView tvHelp;
    private TextView tvCurrentLang;
    MainActivity activity;

    public SettingsFragment() {

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mAuth = FireBaseHelper.getInstance().getAuth();
        bindViews(view);
        init();
        return view;
    }


    private void bindViews(View view) {

        parentLayout = view.findViewById(R.id.layout_settings);

        switchNotification = view.findViewById(R.id.switch_settings_notifications);
        switchNotification.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_notifications_none_black_24dp, 0, 0, 0);

        layoutChangeState = view.findViewById(R.id.layout_settings_change_state);
        tvCurrentState = view.findViewById(R.id.tv_settings_current_state);
        TextView tvChangeState = view.findViewById(R.id.tv_settings_change_state);
        tvChangeState.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_place_black_24dp, 0, 0, 0);

        layoutChangeLang = view.findViewById(R.id.layout_settings_change_lang);
        tvCurrentLang = view.findViewById(R.id.tv_settings_language);
        TextView tvChangeLang = view.findViewById(R.id.tv_settings_change_lang);
        tvChangeLang.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_language_black_24dp, 0, 0, 0);

        tvHelp = view.findViewById(R.id.tv_settings_view_help);
        tvHelp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_live_help_black_24dp, 0, 0, 0);

        TextView tvChangePwd = view.findViewById(R.id.tv_settings_change_password);
        tvChangePwd.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_password, 0, 0, 0);
        layoutChangePwd = view.findViewById(R.id.layout_settings_change_password);

        layoutSignInOut = view.findViewById(R.id.layout_settings_sign_in_out);
        tvSignOut = view.findViewById(R.id.tv_settings_sign_in_out);
        tvAccount = view.findViewById(R.id.tv_settings_account);
        manageSignOut();
    }

    private void init() {

        activity = (MainActivity) getActivity();

        switchNotification.setChecked(Preferences.getInstance().getBooleanPref(getContext(), Preferences.PREF_RECEIVE_NOTIFICATIONS));
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Preferences.getInstance().setBooleanPref(getContext(), Preferences.PREF_RECEIVE_NOTIFICATIONS, true);
            } else {
                Preferences.getInstance().setBooleanPref(getContext(), Preferences.PREF_RECEIVE_NOTIFICATIONS, false);
            }
        });

        String circleName;
        if (Preferences.getInstance().getStringPref(activity, Preferences.PREF_LANGUAGE).equals("hi"))
            circleName = Preferences.getInstance().getCirclePref(activity).getHi();
        else
            circleName = Preferences.getInstance().getCirclePref(activity).getEn();
        tvCurrentState.setText(String.format(getString(R.string.current_circle), circleName));
        layoutChangeState.setOnClickListener(v -> {
            Intent intent = new Intent(activity, StateSettingActivity.class);
            startActivity(intent);
        });

        Locale loc = Locale.getDefault();
        tvCurrentLang.setText(String.format(getString(R.string.language), loc.getDisplayLanguage(loc)));
        layoutChangeLang.setOnClickListener(v -> showLanguageDialog());

        tvHelp.setOnClickListener(v -> {
            Preferences.getInstance().clearPrefs(getContext(), Preferences.PREF_HELP_ONBOARDER);
            startActivity(new Intent(activity, IntroActivity.class).putExtra("FromSettings", true));
        });

        if (Preferences.getInstance().getStaffPref(activity) != null)
            layoutChangePwd.setVisibility(View.VISIBLE);
        else
            layoutChangePwd.setVisibility(View.GONE);

        layoutSignInOut.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                Helper.getInstance().showFancyAlertDialog(getActivity(),
                        getString(R.string.sign_out_from_google),
                        getString(R.string.sign_out),
                        getString(R.string.ok),
                        () -> {
                            activity.signOutFromGoogle();
                            Helper.getInstance().showFancyAlertDialog(getActivity(), "", getString(R.string.signed_out), getString(R.string.ok), () -> {
                            }, null, null, FancyAlertDialogType.SUCCESS);
                            manageSignOut();
                        },
                        getString(R.string.cancel),
                        () -> {
                        },
                        FancyAlertDialogType.WARNING);
            } else {
                activity.signInWithGoogle();
            }
        });


        layoutChangePwd.setOnClickListener(v -> showChangePasswordWindow());
    }

    private void showLanguageDialog() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_select_language, (ViewGroup) getView(), false);
        final RadioGroup rg = v.findViewById(R.id.radio_group_language);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(v)
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                })
                .setPositiveButton(getString(R.string.select), (dialog, which) -> {


                    Helper.getInstance().showReloadWarningDialog(activity, () -> {
                        if (rg.getCheckedRadioButtonId() == R.id.rBEnglish)
                            Preferences.getInstance().setStringPref(getContext(), Preferences.PREF_LANGUAGE, "en");
                        if (rg.getCheckedRadioButtonId() == R.id.rBHindi)
                            Preferences.getInstance().setStringPref(getContext(), Preferences.PREF_LANGUAGE, "hi");
                        Helper.getInstance().reloadApp(activity);
                    });
                });
        builder.show();
        String currentLanguage = Preferences.getInstance().getStringPref(getContext(), Preferences.PREF_LANGUAGE);
        CustomLogger.getInstance().logVerbose("Current Language = " + currentLanguage, CustomLogger.Mask.SETTINGS_FRAGMENT);
        if (currentLanguage.equals(LocaleHelper.ENGLISH)) {
            rg.check(R.id.rBEnglish);
        } else if (currentLanguage.equals(LocaleHelper.HINDI)) {
            rg.check(R.id.rBHindi);
        }
    }

    public void manageSignOut() {
        if (mAuth.getCurrentUser() == null) {
            tvSignOut.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawbale_login_24dp, 0, 0, 0);
            tvSignOut.setText(getString(R.string.sign_in));
            tvAccount.setVisibility(View.INVISIBLE);
            layoutChangePwd.setVisibility(View.GONE);
        } else {
            tvSignOut.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_logout, 0, 0, 0);
            tvSignOut.setText(getString(R.string.sign_out));
            String user = String.format(getString(R.string.signed_in), mAuth.getCurrentUser().getEmail());
            tvAccount.setVisibility(View.VISIBLE);
            tvAccount.setText(user);
        }
    }

    public void showChangePasswordWindow() {

        final EditText editTextOld, editTextNew, editTextConfirm;
        View popupView = LayoutInflater.from(activity).inflate(R.layout.dialog_change_password, (ViewGroup) this.getView(), false);
        final PopupWindow popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        editTextOld = popupView.findViewById(R.id.edittext_old_pwd);
        editTextNew = popupView.findViewById(R.id.edittext_new_pwd);
        editTextConfirm = popupView.findViewById(R.id.edittext_confirm_new_pwd);

        Button change = popupView.findViewById(R.id.btn_change_pwd);
        change.setOnClickListener(v -> {
            String oldPwd = editTextOld.getText().toString();
            String newPwd = editTextNew.getText().toString();
            String confirmPwd = editTextConfirm.getText().toString();
            if (oldPwd.isEmpty()) {
                Toast.makeText(activity, getString(R.string.empty_old), Toast.LENGTH_LONG).show();
            } else if (newPwd.isEmpty()) {
                Toast.makeText(activity, getString(R.string.empty_new), Toast.LENGTH_LONG).show();
            } else if (!confirmPwd.equals(newPwd)) {
                Toast.makeText(activity, getString(R.string.new_not_matching), Toast.LENGTH_LONG).show();
            } else if (FireBaseHelper.getInstance().getAuth().getCurrentUser() == null)
                Helper.getInstance().showErrorDialog(getString(R.string.try_after_signin),
                        getString(R.string.google_signin_first), activity);
            else {
                changePassword(oldPwd, newPwd);
                popupWindow.dismiss();
            }

        });

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.update();
        popupWindow.showAtLocation(parentLayout, Gravity.CENTER, 0, 0);
    }

    private void changePassword(String oldPwd, String newPwd) {

        ProgressDialog progressDialog = Helper.getInstance().getProgressWindow(activity, getString(R.string.please_wait));
        progressDialog.show();
        StaffModel staff = Preferences.getInstance().getStaffPref(activity);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CustomLogger.getInstance().logDebug("onDataChange: " + dataSnapshot.getValue());
                if (dataSnapshot.getValue() != null && dataSnapshot.getValue().equals(oldPwd)) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(FireBaseHelper.ROOT_PASSWORD, newPwd);
                    Task<Void> task = FireBaseHelper.getInstance().updateData(staff.getCircle(),
                            staff.getId(),
                            hashMap,
                            FireBaseHelper.ROOT_STAFF);
                    task.addOnCompleteListener(task1 -> {
                        progressDialog.dismiss();
                        if (task1.isSuccessful()) {
                            Helper.getInstance().showFancyAlertDialog(activity,
                                    "", getString(R.string.password_change_success),
                                    getString(R.string.ok), () -> {
                                    }, null, null, FancyAlertDialogType.SUCCESS);
                        } else {
                            Helper.getInstance().showErrorDialog(getString(R.string.try_again),
                                    getString(R.string.password_change_fail), activity);
                        }
                    });
                } else {
                    progressDialog.dismiss();
                    Helper.getInstance().showErrorDialog(getString(R.string.incorrect_old),
                            getString(R.string.password_change_fail), activity);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Helper.getInstance().showMaintenanceDialog(activity, staff.getCircle());
            }
        };
        FireBaseHelper.getInstance().getDataFromFireBase(staff.getCircle(), valueEventListener,
                true, FireBaseHelper.ROOT_STAFF, staff.getId(), FireBaseHelper.ROOT_PASSWORD);
    }

}
