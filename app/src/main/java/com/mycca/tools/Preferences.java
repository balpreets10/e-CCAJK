package com.mycca.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mycca.models.Circle;
import com.mycca.models.StaffModel;


public class Preferences {

    private static Preferences _instance;

    public static final String PREF_STAFF_DATA = "staffData";
    public static final String PREF_CIRCLE_DATA = "stateData";
    public static final String PREF_RECEIVE_NOTIFICATIONS = "receiveNotifications";
    public static final String PREF_LANGUAGE = "language";
    public static final String PREF_HELP_ONBOARDER = "onBoarder";
    public static final String PREF_CIRCLES = "circles";
    public static final String PREF_ACTIVE_CIRCLES = "activeCircles";
    public static final String PREF_OFFICE_ADDRESS = "officeAddress";
    public static final String PREF_OFFICE_LABEL = "officeLabel";
    public static final String PREF_OFFICE_COORDINATES = "coordinates";
    public static final String PREF_WEBSITE = "website";
    public static final String PREF_GEN_OTP = "otp";

    //    public static final String PREF_HELP_HOME = "home";
//    public static final String PREF_HELP_CONTACT = "contact";
//    public static final String PREF_HELP_INSPECTION = "inspection";
//    public static final String PREF_HELP_GRIEVANCE = "grievance";
//    public static final String PREF_HELP_LOCATOR = "locator";
//    public static final String PREF_HELP_UPDATE = "update";

    public Preferences() {
        _instance = this;
    }

    public static Preferences getInstance() {
        if (_instance == null) {
            return new Preferences();
        } else {
            return _instance;
        }
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }


    /* --------------------basic preferences---------------------- */

    public String getStringPref(Context context, String key) {
        String s = getSharedPreferences(context).getString(key, null);
        if (s == null) {
            if (key.equals(PREF_LANGUAGE))
                return "en";
        }
        return s;
    }

    public void setStringPref(Context context, String key, String value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public int getIntPref(Context context, String key) {
        return getSharedPreferences(context).getInt(key, -1);
    }

    public void setIntPref(Context context, String key, int value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public boolean getBooleanPref(Context context, String key) {
        return getSharedPreferences(context).getBoolean(key, true);
    }

    public void setBooleanPref(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }


    /* --------------------custom preferences---------------------- */

    public void setModelPref(Context context, String key, Object value) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(key, Helper.getInstance().getJsonFromObject(value));
        editor.apply();
    }

    public StaffModel getStaffPref(Context context) {
        String json = getSharedPreferences(context).getString(PREF_STAFF_DATA, null);
        return (StaffModel) Helper.getInstance().getObjectFromJson(json, StaffModel.class);
    }

    public Circle getCirclePref(Context context) {
        String json = getSharedPreferences(context).getString(PREF_CIRCLE_DATA, null);
        return (Circle) Helper.getInstance().getObjectFromJson(json, Circle.class);
    }


    /* --------------------clear preferences---------------------- */

    public void clearPrefs(Context context, String pref) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(pref);
        editor.apply();
    }

    public void clearOtherStateDataPrefs(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(PREF_OFFICE_ADDRESS);
        editor.remove(PREF_OFFICE_LABEL);
        editor.remove(PREF_OFFICE_COORDINATES);
        editor.remove(PREF_WEBSITE);
        editor.apply();
    }

    //    public void clearTutorialPrefs(Context context) {
//        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
//        editor.remove(PREF_HELP_ONBOARDER);
//        editor.remove(PREF_HELP_HOME);
//        editor.remove(PREF_HELP_INSPECTION);
//        editor.remove(PREF_HELP_UPDATE);
//        editor.remove(PREF_HELP_GRIEVANCE);
//        editor.remove(PREF_HELP_LOCATOR);
//        editor.remove(PREF_HELP_CONTACT);
//        editor.apply();
//    }
//
//    public void setTutorialPrefs(Context context) {
//        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
//        editor.putBoolean(PREF_HELP_ONBOARDER, false);
//                editor.putBoolean(PREF_HELP_HOME, false);
//        editor.putBoolean(PREF_HELP_INSPECTION, false);
//        editor.putBoolean(PREF_HELP_UPDATE, false);
//        editor.putBoolean(PREF_HELP_GRIEVANCE, false);
//        editor.putBoolean(PREF_HELP_LOCATOR, false);
//        editor.putBoolean(PREF_HELP_CONTACT, false);
//        editor.apply();
//    }

}
