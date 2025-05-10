package com.mycca.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.mycca.R;
import com.mycca.adapter.GenericSpinnerAdapter;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.MySubmittableAppCompatActivity;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.custom.customImagePicker.ImagePicker;
import com.mycca.custom.customImagePicker.cropper.CropImage;
import com.mycca.custom.customImagePicker.cropper.CropImageView;
import com.mycca.enums.State;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.models.Circle;
import com.mycca.models.SelectedImageModel;
import com.mycca.providers.CircleDataProvider;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.DataSubmissionAndMail;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.LocaleHelper;
import com.mycca.tools.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KypUploadActivity extends MySubmittableAppCompatActivity implements VolleyHelper.VolleyResponse {

    ImageView imageView;
    SelectedImageModel imageModel;
    TextInputEditText editTextMobileNum;
    Button add, submit;
    Spinner circles;
    ImagePicker imagePicker;
    ProgressDialog progressDialog;
    VolleyHelper volleyHelper;
    ArrayList<Uri> firebaseImageURLs = new ArrayList<>();
    String mobile;
    public static final int REQUEST_OTP = 8543;
//    private boolean isUploadedToFirebase = false, isUploadedToServer = false, isOTPVerified = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kyp_upload);
        getSupportActionBar().setTitle(R.string.app_name);
        bindViews();
        init();
    }

    private void bindViews() {
        imageView = findViewById(R.id.imageView_kyp);
        editTextMobileNum = findViewById(R.id.et_kyp_mobile);
        add = findViewById(R.id.add_kyp);
        submit = findViewById(R.id.submit_kyp);
        circles = findViewById(R.id.spinner_kyp_circle);
    }

    private void init() {
        progressDialog = new ProgressDialog(this);
        volleyHelper = new VolleyHelper(this, this);

        GenericSpinnerAdapter<Circle> statesAdapter = new GenericSpinnerAdapter<>(this,
                CircleDataProvider.getInstance().getActiveCircleData());
        circles.setAdapter(statesAdapter);

        add.setOnClickListener(v -> showImageChooser());

        submit.setOnClickListener(v -> {
            mobile = editTextMobileNum.getText().toString().trim();
            if (imageModel != null && mobile.length() == 10)
                checkConnection();
            else {
                if (imageModel == null)
                    Toast.makeText(this, getString(R.string.add_kyp_image), Toast.LENGTH_LONG).show();
                else
                    editTextMobileNum.setError(getString(R.string.invalid_mobile));
            }
        });
    }

    private void checkConnection() {
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                CustomLogger.getInstance().logDebug("version checked = " + Helper.versionChecked, CustomLogger.Mask.KYP_ACTIVITY);
                if (Helper.versionChecked) {
                    submit();
                } else {
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                long value = (long) dataSnapshot.getValue();
                                if (Helper.getInstance().onLatestVersion(value, KypUploadActivity.this))
                                    submit();
                            } catch (Exception e) {
                                Helper.getInstance().showMaintenanceDialog(KypUploadActivity.this, null);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Helper.getInstance().showMaintenanceDialog(KypUploadActivity.this, null);
                        }
                    };
                    FireBaseHelper.getInstance().getDataFromFireBase(null,
                            valueEventListener, true, FireBaseHelper.ROOT_INITIAL_CHECKS, FireBaseHelper.ROOT_APP_VERSION);
                }

            }

            @Override
            public void OnConnectionNotAvailable() {
                Helper.getInstance().noInternetDialog(KypUploadActivity.this);
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    private void submit() {
//        if (isOTPVerified) {
//            if (isUploadedToFirebase) {
//                if (isUploadedToServer)
//                    sendMail();
//                else
//                    uploadImageToServer();
//            } else
//                uploadImageOnFirebase();
//        } else
//            otp();

        switch (state) {
            case INIT: {
                otp();
                break;
            }
            case OTP_VERIFIED: {
                uploadImageOnFirebase();
                break;
            }
            case UPLOADED_TO_FIREBASE: {
                uploadImageToServer();
                break;
            }
            case UPLOADED_TO_SERVER: {
                sendMail();
                break;
            }
        }
    }

    private void otp() {
        Intent intent = new Intent(this, VerificationActivity.class)
                .putExtra(VerificationActivity.INTENT_PHONENUMBER, mobile)
                .putExtra(VerificationActivity.INTENT_COUNTRY_CODE, "91");

        startActivityForResult(intent, REQUEST_OTP);
    }

    private void uploadImageOnFirebase() {
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        Circle circle = (Circle) circles.getSelectedItem();
        UploadTask uploadTask = FireBaseHelper.getInstance().uploadFiles(circle.getCode(),
                imageModel,
                false,
                0,
                FireBaseHelper.ROOT_KYP,
                FireBaseHelper.getInstance().getAuth().getUid());

        if (uploadTask != null) {
            uploadTask.addOnFailureListener(exception -> {
                progressDialog.dismiss();
                Helper.getInstance().showErrorDialog(getString(R.string.file_not_uploaded), getString(R.string.file_upload_error), this);
                CustomLogger.getInstance().logDebug("onFailure: " + exception.getMessage(), CustomLogger.Mask.KYP_ACTIVITY);
            }).addOnSuccessListener(taskSnapshot ->
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        firebaseImageURLs.add(uri);
                        updateState(State.UPLOADED_TO_FIREBASE);
//                        isUploadedToFirebase = true;
                        submit();
                    }));
        }
    }

    private void uploadImageToServer() {

        DataSubmissionAndMail.getInstance().uploadImagesToServer(firebaseImageURLs,
                FireBaseHelper.getInstance().getAuth().getUid(),
                DataSubmissionAndMail.SUBMIT,
                volleyHelper);
    }

    private void sendMail() {

        String identifier = FireBaseHelper.getInstance().getAuth().getUid();
        progressDialog.setMessage(getString(R.string.almost_done));
        String url = Helper.getInstance().getAPIUrl() + "sendInfoUpdateEmail.php/";
        Map<String, String> params = new HashMap<>();

        params.put("Identifier", identifier);
        params.put("folder", DataSubmissionAndMail.SUBMIT);
        //params.put("mailTo",circle.getMails());
        //params.put("updateType", "KYP");

        DataSubmissionAndMail.getInstance().sendMail(params, "send_mail-" + identifier, volleyHelper, url);

    }

    private void showImageChooser() {
        imagePicker = Helper.getInstance().showImageChooser(imagePicker, this, true, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {
                CustomLogger.getInstance().logDebug("onPickImage: " + imageUri.getPath(), CustomLogger.Mask.KYP_ACTIVITY);

            }

            @Override
            public void onCropImage(Uri imageUri) {
                CustomLogger.getInstance().logDebug("onCropImage: " + imageUri.getPath(), CustomLogger.Mask.KYP_ACTIVITY);
                imageModel = new SelectedImageModel(imageUri);
                Glide.with(KypUploadActivity.this).load(imageUri).into(imageView);
            }


            @Override
            public void cropConfig(CropImage.ActivityBuilder builder) {
                builder
                        .setMultiTouchEnabled(false)
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setRequestedSize(720, 1280);
            }

            @Override
            public void onPermissionDenied(int requestCode, String[] permissions,
                                           int[] grantResults) {
                CustomLogger.getInstance().logDebug("onPermissionDenied: Permission not given to choose textViewMessage", CustomLogger.Mask.KYP_ACTIVITY);
            }
        });

    }

    @Override
    public void onError(VolleyError volleyError) {
        progressDialog.dismiss();
        Helper.getInstance().showErrorDialog(getString(R.string.try_again), getString(R.string.some_error), this);
    }

    @Override
    public void onResponse(String str) {
        JSONObject jsonObject = Helper.getInstance().getJson(str);
        CustomLogger.getInstance().logDebug(jsonObject.toString(), CustomLogger.Mask.KYP_ACTIVITY);
        try {
            if (jsonObject.get("action").equals("Creating Image")) {
                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {
                    CustomLogger.getInstance().logDebug("onResponse: Files uploaded", CustomLogger.Mask.KYP_ACTIVITY);
                    updateState(State.UPLOADED_TO_SERVER);
//                    isUploadedToServer = true;
                    submit();
                } else {
                    CustomLogger.getInstance().logDebug("onResponse: Image upload failed", CustomLogger.Mask.KYP_ACTIVITY);
                    progressDialog.dismiss();
                    Helper.getInstance().showErrorDialog(getString(R.string.file_not_uploaded), getString(R.string.file_upload_error), this);
                }
            } else if (jsonObject.getString("action").equals("Sending Mail")) {
                progressDialog.dismiss();
                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {
                    Helper.getInstance().showMessage(this,
                            getString(R.string.kyp_upload_success),
                            getString(R.string.success),
                            FancyAlertDialogType.SUCCESS);
                    updateState(State.INIT);
//                    isUploadedToServer = isUploadedToFirebase = isOTPVerified = false;
                } else {
                    Helper.getInstance().showErrorDialog(getString(R.string.kyp_upload_fail),
                            getString(R.string.failure),
                            this);
                }
            }
        } catch (JSONException jse) {
            jse.printStackTrace();
            progressDialog.dismiss();
            Helper.getInstance().showErrorDialog(getString(R.string.some_error), getString(R.string.try_again), this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (imagePicker != null)
            imagePicker.onActivityResult(this, requestCode, resultCode, data);
        if (requestCode == REQUEST_OTP) {
            if (resultCode == RESULT_OK) {
                CustomLogger.getInstance().logDebug("Verification complete", CustomLogger.Mask.KYP_ACTIVITY);
                updateState(State.OTP_VERIFIED);
//                isOTPVerified = true;
                submit();
            } else {
                Helper.getInstance().showErrorDialog(getString(R.string.try_again), getString(R.string.failed), this);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (imagePicker != null)
            imagePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

    }

    @Override
    public void updateState(State state) {
        this.state = state;
    }
}
