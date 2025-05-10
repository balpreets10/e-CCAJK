
package com.mycca.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.mycca.R;
import com.mycca.activity.SplashActivity;
import com.mycca.app.AppController;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialog;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.FancyAlertDialog.IFancyAlertDialogListener;
import com.mycca.custom.FancyAlertDialog.Icon;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.custom.customImagePicker.ImagePicker;
import com.mycca.listeners.WriteFileCompletionListener;
import com.mycca.models.GrievanceType;
import com.mycca.models.SelectedImageModel;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Helper {

    private static Helper _instance;

    public static boolean versionChecked = false;

    private GrievanceType pensionGrievanceTypes[] = {
            new GrievanceType(AppController.getResourses().getString(R.string.change_of_pda), 0),
            new GrievanceType(AppController.getResourses().getString(R.string.correction_in_ppo), 1),
            new GrievanceType(AppController.getResourses().getString(R.string.wrong_fixation), 2),
            new GrievanceType(AppController.getResourses().getString(R.string.non_updation_da), 3),
            new GrievanceType(AppController.getResourses().getString(R.string.non_payment_monthly), 4),
            new GrievanceType(AppController.getResourses().getString(R.string.non_payment_medical), 5),
            new GrievanceType(AppController.getResourses().getString(R.string.non_starting_pension), 6),
            new GrievanceType(AppController.getResourses().getString(R.string.non_revision), 7),
            new GrievanceType(AppController.getResourses().getString(R.string.request_cgies), 8),
            new GrievanceType(AppController.getResourses().getString(R.string.excess_short_payment), 9),
            new GrievanceType(AppController.getResourses().getString(R.string.enhancement_on_75_80), 10),
            new GrievanceType(AppController.getResourses().getString(R.string.other_pension_gr), 11)
    };

    private GrievanceType gpfGrievanceTypes[] = {
            new GrievanceType(AppController.getResourses().getString(R.string.gpf_final_not_received), 100),
            new GrievanceType(AppController.getResourses().getString(R.string.correction_name), 101),
            new GrievanceType(AppController.getResourses().getString(R.string.change_nomination), 102),
            new GrievanceType(AppController.getResourses().getString(R.string.gpf_acc_not_transferred), 103),
            new GrievanceType(AppController.getResourses().getString(R.string.details_of_gpf_deposit), 104),
            new GrievanceType(AppController.getResourses().getString(R.string.non_payment_gpf_withdrawal), 105),
            new GrievanceType(AppController.getResourses().getString(R.string.other_gpf_gr), 106)
    };

    public Helper() {
        _instance = this;
    }

    public static Helper getInstance() {
        if (_instance == null) {
            return new Helper();
        } else {
            return _instance;
        }
    }

    public static void resetInstance() {
        _instance = null;
    }

    public String getPlayStoreURL() {
        return "market://details?id=com.mycca";
    }

    String getConnectionCheckURL() {
        return "https://www.google.co.in/";
    }

    public String getAPIUrl() {
        boolean debugMode = true;
        if (debugMode) {
            return "http://jknccdirectorate.com/api/cca/debug/v1/";
        } else {
            return "http://jknccdirectorate.com/api/cca/release/v1/";
        }

    }

    public GrievanceType[] getPensionGrievanceTypeList() {
        return pensionGrievanceTypes;
    }

    public GrievanceType[] getGPFGrievanceTypeList() {
        return gpfGrievanceTypes;
    }

    public GrievanceType getGrievanceFromId(long id) {
        for (GrievanceType grievanceType : pensionGrievanceTypes)
            if (grievanceType.getId() == id)
                return grievanceType;
        for (GrievanceType grievanceType : gpfGrievanceTypes)
            if (grievanceType.getId() == id)
                return grievanceType;
        return null;
    }

    private Context getLocalisedContext(Locale desiredLocale) {
        Configuration conf = AppController.getInstance().getResources().getConfiguration();
        conf = new Configuration(conf);
        conf.setLocale(desiredLocale);
        return AppController.getInstance().createConfigurationContext(conf);
    }

    public String getGrievanceString(long id, Locale desiredLocale) {
        Resources res;
        Context localizedContext = getLocalisedContext(desiredLocale);
        res = localizedContext.getResources();

        switch ((int) id) {
            case 0:
                return res.getString(R.string.change_of_pda);
            case 1:
                return res.getString(R.string.correction_in_ppo);
            case 2:
                return res.getString(R.string.wrong_fixation);
            case 3:
                return res.getString(R.string.non_updation_da);
            case 4:
                return res.getString(R.string.non_payment_monthly);
            case 5:
                return res.getString(R.string.non_payment_medical);
            case 6:
                return res.getString(R.string.non_starting_pension);
            case 7:
                return res.getString(R.string.non_revision);
            case 8:
                return res.getString(R.string.request_cgies);
            case 9:
                return res.getString(R.string.excess_short_payment);
            case 10:
                return res.getString(R.string.enhancement_on_75_80);
            case 11:
                return res.getString(R.string.other_pension_gr);
            case 100:
                return res.getString(R.string.gpf_final_not_received);
            case 101:
                return res.getString(R.string.correction_name);
            case 102:
                return res.getString(R.string.change_nomination);
            case 103:
                return res.getString(R.string.gpf_acc_not_transferred);
            case 104:
                return res.getString(R.string.details_of_gpf_deposit);
            case 105:
                return res.getString(R.string.non_payment_gpf_withdrawal);
            case 106:
                return res.getString(R.string.other_gpf_gr);
        }
        return null;
    }

    public String getGrievanceCategory(long id, Locale desiredLocale) {
        Resources res;
        Context localizedContext = getLocalisedContext(desiredLocale);
        res = localizedContext.getResources();

        if (id < 100)
            return res.getString(R.string.pension);
        else
            return res.getString(R.string.gpf);
    }

    public String getStatusString(long status, Locale desiredLocale) {
        Resources res;
        Context localizedContext = getLocalisedContext(desiredLocale);
        res = localizedContext.getResources();

        switch ((int) status) {
            case 0:
                return res.getString(R.string.submitted);
            case 1:
                return res.getString(R.string.under_process);
            case 2:
                return res.getString(R.string.resolved);
        }
        return null;
    }

    public String getEnglishString(int resId) {
        if (Locale.getDefault() != Locale.ENGLISH) {
            Context localizedContext = getLocalisedContext(Locale.ENGLISH);
            return localizedContext.getResources().getString(resId);
        } else
            return AppController.getResourses().getString(resId);
    }

    public String formatDate(Date date, String format) {
        SimpleDateFormat dt = new SimpleDateFormat(format, Locale.getDefault());
        return dt.format(date);
    }

    public class DateFormat {
        public static final String DD_MM_YYYY = "dd MMM, yyyy";
    }

    public InputFilter[] limitInputLength(int length) {
        return new InputFilter[]{new InputFilter.LengthFilter(length)};
    }

    public boolean onLatestVersion(long newVersion, final Activity activity) {
        int version = getAppVersion(activity);
        if (newVersion == version) {
            versionChecked = true;
            return true;
        } else {
            showUpdateDialog(activity);
        }
        return false;
    }

    public int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void showGooglePlayStore(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getPlayStoreURL()));
        activity.startActivity(intent);

    }

    public String getJsonFromObject(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public Object getObjectFromJson(String json, Type type) {
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    public ArrayList getCollectionFromJson(String json, Type collectionType) {
        Gson gson = new Gson();
        return gson.fromJson(json, collectionType);
    }

    private File saveFileToStorage(SelectedImageModel imageModel) {

        File file = new File(AppController.getInstance().getFilesDir(), imageModel.getFile().getName());

        BufferedInputStream bis;
        BufferedOutputStream bos;
        try {
            bis = new BufferedInputStream(new FileInputStream(imageModel.getFile()));
            bos = new BufferedOutputStream(new FileOutputStream(file, false));
            byte[] buf = new byte[1024];
            bis.read(buf);
            do {
                bos.write(buf);
            } while (bis.read(buf) != -1);
            bis.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public void deleteFilesFromStorage(String filepaths) {
        if (filepaths != null) {
            String[] intermediate = filepaths.split(",");
            for (String path : intermediate) {
                File file = new File(path);
                boolean deleted = file.delete();
                if (deleted)
                    CustomLogger.getInstance().logDebug("File deleted");
                else
                    CustomLogger.getInstance().logDebug("File not deleted");
            }
        }
    }

    public String getStringFromList(ArrayList<SelectedImageModel> list) {
        StringBuffer fileList = new StringBuffer();
        if (list.size() > 0) {
            for (SelectedImageModel imageModel : list) {
                File to = saveFileToStorage(imageModel);
                fileList = fileList.append(Uri.parse(to.getPath())).append(",");
            }
            fileList.deleteCharAt(fileList.length() - 1);
        }
        return fileList.toString();
    }

    public List<SelectedImageModel> getImagesFromString(String cachedPath) {
        if (cachedPath.isEmpty())
            return null;
        ArrayList<SelectedImageModel> arrayList = new ArrayList<>();
        String[] intermediate = cachedPath.split(",");
        for (String path : intermediate) {
            CustomLogger.getInstance().logDebug(path);
            SelectedImageModel imageModel = new SelectedImageModel(Uri.parse("file://" + path));
            arrayList.add(imageModel);
        }
        return arrayList;
    }

    /*public String getByteArraysFromList(ArrayList<SelectedImageModel> list) {
        StringBuffer fileList = new StringBuffer();
        if (list.size() > 0) {
            for (SelectedImageModel imageModel : list) {
                try {
                    byte[] bytes = FileUtils.readFileToByteArray(imageModel.getFile());
                    CustomLogger.getInstance().logDebug("byte array = " + bytes);
                    fileList = fileList.append(bytes).append(",");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            fileList.deleteCharAt(fileList.length() - 1);
            CustomLogger.getInstance().logDebug("final  array = " + fileList);
        }
        return fileList.toString();
    }

    public List<SelectedImageModel> getImagesFromByteArrays(String string,Context context) {
        if (string.isEmpty())
            return null;
        int i = 0;
        ArrayList<SelectedImageModel> arrayList = new ArrayList<>();
        String[] intermediate = string.split(",");
        for (String byteArray : intermediate) {
            try {
                byte[] inter = Base64.decode(byteArray, Base64.DEFAULT);
                File temp = new File(context.getCacheDir(),"File" + i);
                FileUtils.writeByteArrayToFile(temp, inter);
                SelectedImageModel imageModel = new SelectedImageModel(temp);
                arrayList.add(imageModel);
                CustomLogger.getInstance().logDebug("current file = " ,byteArray);
                i++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }
*/

    public void saveModelOffline(Activity context, Object model, Type collectionType, String filename) {
        IOHelper.getInstance().readFromFile(context, filename, null, jsonObject -> {
            ArrayList arrayList = new ArrayList<>();
            if (jsonObject != null)
                arrayList = getCollectionFromJson(jsonObject.toString(), collectionType);
            CustomLogger.getInstance().logDebug("\nJson array = " + arrayList);
            arrayList.add(model);
            CustomLogger.getInstance().logDebug("\nJson array after addition = " + arrayList);
            String newJson = getJsonFromObject(arrayList);
            IOHelper.getInstance().writeToFile(context, newJson, filename, null, success -> {
                if (success)
                    showMessage(context, "", context.getString(R.string.data_save_success), FancyAlertDialogType.SUCCESS);
                else
                    showErrorDialog(context.getString(R.string.try_again), context.getString(R.string.data_save_fail), context);
            });
        });
    }

    public void deleteOfflineModel(Activity context, int pos, ArrayList arrayList,
                                   String filename, WriteFileCompletionListener writeFileCompletionListener) {
        arrayList.remove(pos);
        CustomLogger.getInstance().logDebug("\narrayList after deletion = " + arrayList);
        String newJson = getJsonFromObject(arrayList);
        IOHelper.getInstance().writeToFile(context, newJson, filename, null, writeFileCompletionListener);
    }

    public ProgressDialog getProgressWindow(final Activity context, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    public boolean checkEmptyInput(String input) {

        CustomLogger.getInstance().logDebug("checkEmptyInput: = " + input);
        boolean result;
        result = (input == null || input.trim().isEmpty());
        CustomLogger.getInstance().logDebug("checkEmptyInput: result = " + result);
        return result;
    }

    public void hideKeyboardFrom(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = activity.getCurrentFocus();
        if (inputMethodManager != null && focusedView != null) {
            inputMethodManager.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    public ImagePicker showImageChooser(ImagePicker imagePicker, Activity activity, boolean cropimage, ImagePicker.Callback callback) {
        if (imagePicker == null) {
            imagePicker = new ImagePicker();
        }
        imagePicker.setTitle(activity.getString(R.string.pick_image_intent_chooser_title));
        imagePicker.setCropImage(cropimage);
        imagePicker.startChooser(activity, callback);
        return imagePicker;
    }

    public boolean isTab(Context context) {
        boolean isTab = (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        CustomLogger.getInstance().logDebug("Tab = " + isTab);
        return isTab;
    }

    public void showFancyAlertDialog(Activity activity,
                                     String message,
                                     String title,
                                     String positiveButtonText, IFancyAlertDialogListener positiveButtonOnClickListener,
                                     String negativeButtonText, IFancyAlertDialogListener negativeButtonOnClickListener,
                                     FancyAlertDialogType fancyAlertDialogType) {
        if (title == null) {
            title = activity.getString(R.string.app_name);
        }
        if (message == null) {
            CustomLogger.getInstance().logDebug("showFancyAlertDialog: Message cant be null");
            return;
        }
        int bgColor = 1;
        int icon = 1;
        switch (fancyAlertDialogType) {
            case ERROR:
                bgColor = Color.parseColor("#aa0000");
                icon = R.drawable.ic_sentiment_dissatisfied_black_24dp;
                break;
            case SUCCESS:
                bgColor = Color.parseColor("#00aa00");
                icon = R.drawable.ic_check_black_24dp;
                break;
            case WARNING:
                bgColor = Color.parseColor("#E2AB04");
                icon = R.drawable.ic_error_outline_black_24dp;
                break;
        }

        new FancyAlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setBackgroundColor(bgColor)
                .setNegativeBtnText(negativeButtonText)
                .setPositiveBtnText(positiveButtonText)
                .isCancellable(false)
                .setPositiveBtnBackground(bgColor)
                .setIcon(icon, Icon.Visible)
                .OnNegativeClicked(negativeButtonOnClickListener)
                .OnPositiveClicked(positiveButtonOnClickListener)
                .build();
    }


    private void showUpdateDialog(final Activity activity) {
        showFancyAlertDialog(activity,
                activity.getString(R.string.update_available),
                activity.getString(R.string.app_name),
                activity.getString(R.string.update),
                () -> {
                    showGooglePlayStore(activity);
                    activity.finish();
                },
                activity.getString(R.string.cancel),
                activity::finish,
                FancyAlertDialogType.WARNING);

    }

    public void showMaintenanceDialog(Activity activity, String state) {
        if (state == null)
            showDialog(activity, activity.getString(R.string.app_maintenance));
        else {
            FireBaseHelper.getInstance().getDataFromFireBase(state, new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String string;
                    if ((boolean) dataSnapshot.getValue())
                        string = activity.getString(R.string.app_maintenance);
                    else
                        string = activity.getString(R.string.state_n_a);
                    showDialog(activity, string);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            }, true, FireBaseHelper.ROOT_ACTIVE);
        }
    }

    private void showDialog(Activity activity, String string) {
        showFancyAlertDialog(activity,
                string,
                activity.getString(R.string.app_name),
                activity.getString(R.string.ok),
                () -> {
                }, null, null,
                FancyAlertDialogType.WARNING);
    }

    public void showMessage(Activity activity, String message, String title, FancyAlertDialogType dialogType) {
        showFancyAlertDialog(activity, message, title,
                activity.getString(R.string.ok), () -> {
                }, null, null, dialogType);

    }

    public void noInternetDialog(Activity activity) {
        Helper.getInstance().showFancyAlertDialog(activity,
                activity.getString(R.string.connect_to_internet),
                activity.getString(R.string.no_internet),
                activity.getString(R.string.ok), () -> {
                }, null, null, FancyAlertDialogType.ERROR);
    }

    public void showErrorDialog(String message, String title, Activity activity) {
        showFancyAlertDialog(activity,
                message,
                title,
                activity.getString(R.string.ok),
                null,
                null,
                null,
                FancyAlertDialogType.ERROR);
    }

    public void showReloadWarningDialog(Activity context, IFancyAlertDialogListener positiveButtonOnClickListener) {
        showFancyAlertDialog(context,
                context.getString(R.string.reload_req),
                context.getString(R.string.reload_app),
                context.getString(R.string.reload),
                positiveButtonOnClickListener,
                context.getString(R.string.cancel),
                () -> {
                },
                FancyAlertDialogType.WARNING);
    }

    public void reloadApp(Activity activity) {
        Intent intent = new Intent(activity, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        resetInstance();
    }

    public void getConfirmationDialog(Activity context, View view, DialogInterface.OnClickListener yes) {
        AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
        confirmDialog.setView(view);
        confirmDialog.setPositiveButton(context.getString(R.string.confirm), yes);
        confirmDialog.setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        confirmDialog.show();
    }

    public JSONObject getJson(String input) {
        try {
            try {
                return new JSONObject(input.substring(input.indexOf("{"), input.indexOf("}") + 1));
            } catch (JSONException jse) {
                jse.printStackTrace();
                CustomLogger.getInstance().logVerbose("getJson: Error creating json");
                return null;
            }
        } catch (StringIndexOutOfBoundsException sioobe) {
            sioobe.printStackTrace();
            return null;
        }
    }

    private byte[] getByteArrayFromBitmap(Bitmap image) {
        if (image == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] getByteArrayFromFile(File file) {
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getBitmapFromString(String value) {
        byte[] inter = Base64.decode(value, 0);
        CustomLogger.getInstance().logDebug("Byte Array = " + inter.toString());
        return BitmapFactory.decodeByteArray(inter, 0, inter.length);
    }

    public byte[] getByteArrayFromBitmapFile(String path) {
        if (path != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            return getByteArrayFromBitmap(bitmap);
        }
        return null;

    }

    public Bitmap createBitmapFromByteArray(byte[] array) {
        if (array != null) {
            return BitmapFactory.decodeByteArray(array, 0, array.length);
        }
        return null;
    }

    public Bitmap getBitmapFromByteArray(byte[] value) {
        if (value != null) {
            return BitmapFactory.decodeByteArray(value, 0, value.length);
        }
        return null;
    }

    public Bitmap getBitmapFromResource(Context context, int res) {
        return BitmapFactory.decodeResource(context.getResources(), res);
    }

    public void showSnackBar(CharSequence message, View view) {
        Snackbar.make(view.findViewById(R.id.fragmentPlaceholder), message, Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", v -> CustomLogger.getInstance().logVerbose("Yes Clicked"))
                .show();
    }


   /*
    public void remove() {
        DatabaseReference versionedDbRef = FireBaseHelper.getInstance().versionedDbRef;
        versionedDbRef.child("Locations").removeValue();
    }*/

   /* public void updateLocations() {
        DatabaseReference versionedDbRef = FirebaseDatabase.getInstance().getReference().child(FireBaseHelper.ROOT_WIFI).child("05");
        versionedDbRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                HashMap<String,Object> hashMap=new HashMap<>();
                hashMap.put("StateID", "05");
                FirebaseDatabase.getInstance().getReference().child(FireBaseHelper.ROOT_WIFI).child("05")
                        .child(dataSnapshot.getKey()).updateChildren(hashMap);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }*/

}

