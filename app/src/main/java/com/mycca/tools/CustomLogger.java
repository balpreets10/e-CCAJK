package com.mycca.tools;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CustomLogger {

    private static CustomLogger _instance;
    private final String TAG = "Custom Logger";
    private static boolean logEnabled = true;

    private void enableMasks() {
        removeAllMasks();
        mMasks.add(Mask.LOCALE_HELPER);
        mMasks.add(Mask.SETTINGS_FRAGMENT);
    }

    private List<Mask> mMasks;

    private CustomLogger() {
        if (mMasks == null)
            mMasks = new ArrayList<>();
        _instance = this;
        enableMasks();
    }

    public static CustomLogger getInstance() {
        if (_instance == null) {
            return new CustomLogger();
        } else {
            return _instance;
        }
    }

    public enum Mask {
        NONE,
        KYP_ACTIVITY,
        NEWS_ACTIVITY,
        MAIN_ACTIVITY,
        SPLASH_ACTIVITY,
        STATE_SETTING_ACTIVITY,
        TRACK_GRIEVANCE_ACTIVITY,
        UPDATE_GRIEVANCE_ACTIVITY,
        VERIFICATION_ACTIVITY,
        BARCODE_CAPTURE_ACTIVITY,

        VOLLEY,
        IMAGE_PICKER,
        FAB_REVEAL_MENU,
        CROP_OVERLAY,

        RECYCLER_VIEW_SELECTED_IMAGES,
        RECYCLER_VIEW_CONTACTS,
        RECYCLER_VIEW_GRIEVANCE_UPDATE,

        APP_CONTROLLER,


        CAMERA_SOURCE,
        CAMERA_SOURCE_PREVIEW,
        VIEW_ANIMATION_UTILS,
        VIEW_PAGER_EX,
        INFINITE_PAGER,
        FIREBASE,

        BROWSER_FRAGMENT,
        CONTACT_US_FRAGMENT,
        FEEDBACK_FRAGMENT,
        ADD_NEWS_FRAGMENT,
        HOME_FRAGMENT,
        INSPECTION_FRAGMENT,
        LOCATOR_FRAGMENT,
        LOGIN_FRAGMENT,
        PAN_AADHAR_FRAGMENT,
        SAVED_MODELS_LIST_FRAGMENT,


        BITMAP_UTILS,
        MY_LOCATION_MANAGER, LOCALE_HELPER, SETTINGS_FRAGMENT,

    }

    public void addMask(Mask inMask) {
        mMasks.add(inMask);
    }

    public void removeMask(Mask mask) {
        if (mMasks.contains(mask))
            mMasks.remove(mask);

    }

    public void removeAllMasks() {
        if (mMasks != null)
            mMasks.clear();
    }

    public void logVerbose(String message, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.v(TAG, message);
    }

    public void logVerbose(String message) {
        if (logEnabled)
            Log.v(TAG, message);
    }

    public void logVerbose(String tag, String message, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.v(tag, message);
    }

    public void logVerbose(String tag, String message) {
        if (logEnabled)
            Log.v(tag, message);
    }

    public void logDebug(String message, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.d(TAG, message);
    }

    public void logDebug(String message) {
        if (logEnabled)
            Log.d(TAG, message);
    }

    public void logDebug(String tag, String message, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.d(tag, message);
    }

    public void logDebug(String tag, String message) {
        if (logEnabled)
            Log.d(tag, message);
    }

    public void logInfo(String message, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.i(TAG, message);
    }

    public void logInfo(String message) {
        if (logEnabled)
            Log.i(TAG, message);
    }

    public void logInfo(String tag, String message, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.i(tag, message);
    }

    public void logWarn(String message, Throwable e, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.w(TAG, "warning: " + message, e);
    }

    public void logWarn(String message, Throwable e) {
        if (logEnabled)
            Log.w(TAG, "warning: " + message, e);
    }

    public void logWarn(String tag, String message, Throwable e, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.w(tag, "warning: " + message, e);
    }

    public void logWarn(String tag, String message, Throwable e) {
        if (logEnabled)
            Log.w(tag, "warning: " + message, e);
    }

    public void logError(String message, Throwable e, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.e(TAG, message, e);
    }

    public void logError(String message, Throwable e) {
        if (logEnabled)
            Log.e(TAG, message, e);
    }

    public void logError(String tag, String message, Throwable e, Mask mask) {
        if (logEnabled)
            if (mMasks.contains(mask))
                Log.e(tag, message, e);
    }

    public void logError(String tag, String message, Throwable e) {
        if (logEnabled)
            Log.e(tag, message, e);
    }

}
