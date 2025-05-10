package com.mycca.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.mycca.R;
import com.mycca.adapter.GenericSpinnerAdapter;
import com.mycca.adapter.RecyclerViewAdapterGrievanceUpdate;
import com.mycca.adapter.RecyclerViewAdapterSelectedImages;
import com.mycca.custom.FabRevealMenu.FabListeners.OnFABMenuSelectedListener;
import com.mycca.custom.FabRevealMenu.FabModel.FABMenuItem;
import com.mycca.custom.FabRevealMenu.FabView.FABRevealMenu;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.MySubmittableAppCompatActivity;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.custom.customImagePicker.ImagePicker;
import com.mycca.custom.customImagePicker.cropper.CropImage;
import com.mycca.custom.customImagePicker.cropper.CropImageView;
import com.mycca.enums.State;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.models.GrievanceModel;
import com.mycca.models.SelectedImageModel;
import com.mycca.models.StatusModel;
import com.mycca.notification.Constants;
import com.mycca.notification.FirebaseNotificationHelper;
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
import java.util.Locale;
import java.util.Map;

public class UpdateGrievanceActivity extends MySubmittableAppCompatActivity implements VolleyHelper.VolleyResponse, OnFABMenuSelectedListener {

    int counterUpload = 0;
    int counterServerImages = 0;
    long status;
    String message, grievanceString;

    TextView tvNumberType, textViewPensionerCode, textViewRefNo, textViewGrievanceString, textViewDateOfApplication, textViewAttachedFileCount;
    Spinner statusSpinner;
    EditText editTextMessage;
    Button update;
    FABRevealMenu fabMenu;
    ProgressDialog progressDialog;
    FloatingActionButton buttonAttachFile;
    ImagePicker imagePicker;
    RecyclerView recyclerViewAttachments;

    GrievanceModel grievanceModel;
    VolleyHelper volleyHelper;
    RecyclerViewAdapterSelectedImages adapterSelectedImages;
    ArrayList<SelectedImageModel> attachmentModelArrayList;
    private ArrayList<FABMenuItem> items;
    ArrayList<Uri> fireBaseImageURLs;
    StatusModel[] statusArray;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_grievance);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.app_name);
        grievanceModel = (GrievanceModel) Helper.getInstance().getObjectFromJson(getIntent().getStringExtra("Model"), GrievanceModel.class);
        grievanceString = Helper.getInstance().getGrievanceString(grievanceModel.getGrievanceType(), Locale.getDefault());
        bindViews();
        init();
        setLayoutData();
    }

    private void bindViews() {
        tvNumberType = findViewById(R.id.textview_number_type);
        textViewPensionerCode = findViewById(R.id.textview_pensioner);
        textViewRefNo = findViewById(R.id.textview_reference_number);
        textViewGrievanceString = findViewById(R.id.textview_confirm5);
        textViewDateOfApplication = findViewById(R.id.textview_date);
        statusSpinner = findViewById(R.id.spinner_status);
        editTextMessage = findViewById(R.id.edittext_message);
        update = findViewById(R.id.button_update);
        progressDialog = Helper.getInstance().getProgressWindow(this, getString(R.string.fetching_grievance));
        recyclerViewAttachments = findViewById(R.id.recycler_view_update_grievance_attachments);
        buttonAttachFile = findViewById(R.id.button_attach_update_grievance);
        textViewAttachedFileCount = findViewById(R.id.textview_selected_file_count_update);
    }

    private void init() {
        if (grievanceModel.getGrievanceStatus() == 0) {
            statusArray = new StatusModel[]{new StatusModel(1, getString(R.string.under_process)),
                    new StatusModel(2, getString(R.string.resolved))};
        } else if (grievanceModel.getGrievanceStatus() == 1) {
            statusArray = new StatusModel[]{new StatusModel(2, getString(R.string.resolved))};
        }
        GenericSpinnerAdapter<StatusModel> arrayAdapter = new GenericSpinnerAdapter<>(this, statusArray);
        statusSpinner.setAdapter(arrayAdapter);
        volleyHelper = new VolleyHelper(this, this);
        update.setOnClickListener(v -> startGrievanceUpdate());

        attachmentModelArrayList = new ArrayList<>();
        adapterSelectedImages = new RecyclerViewAdapterSelectedImages(attachmentModelArrayList, this);
        recyclerViewAttachments.setAdapter(adapterSelectedImages);
        recyclerViewAttachments.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        fireBaseImageURLs = new ArrayList<>();
        setSelectedFileCount(0);
        initItems();
        fabMenu = findViewById(R.id.fabMenu_Update);
        try {
            if (buttonAttachFile != null && fabMenu != null) {
                fabMenu.setMenuItems(items);
                fabMenu.bindAnchorView(buttonAttachFile);
                fabMenu.setOnFABMenuSelectedListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initItems() {
        items = new ArrayList<>();
        items.add(new FABMenuItem(0, getString(R.string.add_image), AppCompatResources.getDrawable(this, R.drawable.ic_attach_file_white_24dp)));
        items.add(new FABMenuItem(1, getString(R.string.remove_all), AppCompatResources.getDrawable(this, R.drawable.ic_close_24dp)));
    }

    private void setLayoutData() {
        String identifierType;
        switch (grievanceModel.getIdentifierType()) {
            case R.string.hr_num:
                identifierType = getString(R.string.hr_num);
                break;
            case R.string.staff_num:
                identifierType = getString(R.string.staff_num);
                break;
            default:
                identifierType = getString(R.string.p_code);
        }
        tvNumberType.setText(identifierType);
        textViewPensionerCode.setText(grievanceModel.getIdentifierNumber());
        textViewRefNo.setText(grievanceModel.getReferenceNo());
        textViewGrievanceString.setText(grievanceString);
        textViewDateOfApplication.setText(Helper.getInstance().formatDate(grievanceModel.getDate(), Helper.DateFormat.DD_MM_YYYY));
        editTextMessage.setText(grievanceModel.getMessage() == null ? getResources().getString(R.string.n_a) : grievanceModel.getMessage());
    }

    private void startGrievanceUpdate() {
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                if (Helper.versionChecked) {
                    doUpdateOnInternetAvailable();
                } else {
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                long value = (long) dataSnapshot.getValue();
                                if (Helper.getInstance().onLatestVersion(value, UpdateGrievanceActivity.this))
                                    doUpdateOnInternetAvailable();
                            } catch (Exception e) {
                                Helper.getInstance().showMaintenanceDialog(UpdateGrievanceActivity.this, null);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Helper.getInstance().showMaintenanceDialog(UpdateGrievanceActivity.this, null);
                        }
                    };
                    FireBaseHelper.getInstance().getDataFromFireBase(null, valueEventListener, true, FireBaseHelper.ROOT_INITIAL_CHECKS, FireBaseHelper.ROOT_APP_VERSION);
                }
            }

            @Override
            public void OnConnectionNotAvailable() {
                Helper.getInstance().noInternetDialog(UpdateGrievanceActivity.this);
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    private void doUpdateOnInternetAvailable() {

        switch (state) {
            case INIT: {
                updateGrievanceDataOnFirebase();
                break;
            }

            case UPLOADED_TO_FIREBASE: {
                uploadImagesToServer();
                break;
            }
            case UPLOADED_TO_SERVER: {
                sendFinalMail();
                break;
            }
        }
        //        if (isUploadedToFireBase) {
//            if (isUploadedToServer) {
//                CustomLogger.getInstance().logDebug("doUpdateOnInternetAvailable: Data uploaded on server", CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
//                sendFinalMail();
//            } else {
//                uploadImagesToServer();
//            }
//        } else {
//            updateGrievanceDataOnFirebase();
//        }

    }

    private void updateGrievanceDataOnFirebase() {
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        status = ((StatusModel) statusSpinner.getSelectedItem()).getStatusCode();
        message = editTextMessage.getText().toString().trim();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("grievanceStatus", status);
        hashMap.put("message", message);

        Task<Void> task = FireBaseHelper.getInstance().updateData(grievanceModel.getCircle(),
                String.valueOf(grievanceModel.getGrievanceType()),
                hashMap,
                FireBaseHelper.ROOT_GRIEVANCES,
                grievanceModel.getIdentifierNumber());
        task.addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                uploadAllImagesToFirebase();
            } else {
                progressDialog.dismiss();
                Helper.getInstance().showMaintenanceDialog(UpdateGrievanceActivity.this, grievanceModel.getCircle());
                CustomLogger.getInstance().logDebug("onComplete: " + task1.toString(), CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
            }
        });

    }

    private void uploadAllImagesToFirebase() {
        if (attachmentModelArrayList.size() > 0) {
            progressDialog.setMessage(getString(R.string.uploading_files));
            CustomLogger.getInstance().logDebug("uploadAllImagesToFirebase: uploading", CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
            counterUpload = 0;

            for (int i = 0; i < attachmentModelArrayList.size(); i++) {
                final UploadTask uploadTask = FireBaseHelper.getInstance().uploadFiles(grievanceModel.getCircle(),
                        attachmentModelArrayList.get(i),
                        true,
                        i,
                        FireBaseHelper.ROOT_GRIEVANCES,
                        grievanceModel.getIdentifierNumber(),
                        String.valueOf(grievanceModel.getGrievanceType()),
                        FireBaseHelper.ROOT_BY_STAFF);

                if (uploadTask != null) {
                    uploadTask.addOnFailureListener(exception -> onFailure(getString(R.string.file_not_uploaded), getString(R.string.file_upload_error)))
                            .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                fireBaseImageURLs.add(uri);
                                progressDialog.setMessage(String.format(getString(R.string.uploaded_file), String.valueOf(++counterUpload), String.valueOf(attachmentModelArrayList.size())));
                                if (counterUpload == attachmentModelArrayList.size()) {
                                    updateState(State.UPLOADED_TO_FIREBASE);
                                    doUpdateOnInternetAvailable();
                                }
                            }));
                }
            }
        } else {
            updateState(State.UPLOADED_TO_FIREBASE);
            doUpdateOnInternetAvailable();
        }
    }

    private void uploadImagesToServer() {

        counterServerImages = 0;
        progressDialog.setMessage(getString(R.string.processing));
        int totalFilesToAttach = attachmentModelArrayList.size();
        if (totalFilesToAttach != 0) {

            DataSubmissionAndMail.getInstance().uploadImagesToServer(fireBaseImageURLs,
                    grievanceModel.getIdentifierNumber(),
                    DataSubmissionAndMail.UPDATE,
                    volleyHelper);
        } else {
            updateState(State.UPLOADED_TO_SERVER);
            doUpdateOnInternetAvailable();
        }
    }

    private void sendFinalMail() {

        progressDialog.setMessage(getString(R.string.almost_done));
        String url = Helper.getInstance().getAPIUrl() + "sendUpdateGrievanceEmail.php/";
        Map<String, String> params = new HashMap<>();
        String pensionerCode = grievanceModel.getIdentifierNumber();

//        String personType = Helper.getInstance().getEnglishString(grievanceModel.getIdentifierType());
//        params.put("personType", personType);  //person type is pensioner/hr/staff

        params.put("pensionerCode", pensionerCode);
        params.put("folder", DataSubmissionAndMail.UPDATE);
        params.put("pensionerEmail", grievanceModel.getEmail());
        params.put("status", Helper.getInstance().getStatusString(status, Locale.ENGLISH));
        params.put("refNo", grievanceModel.getReferenceNo());
        params.put("grievanceType", Helper.getInstance().getGrievanceCategory(grievanceModel.getGrievanceType(), Locale.ENGLISH));
        params.put("grievanceSubType", Helper.getInstance().getGrievanceString(grievanceModel.getGrievanceType(), Locale.ENGLISH));
        params.put("message", message);
        params.put("fileCount", attachmentModelArrayList.size() + "");
        DataSubmissionAndMail.getInstance().sendMail(params, "send_mail-" + pensionerCode, volleyHelper, url);
    }

    private void revertUpdatesOnFireBase() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("grievanceStatus", grievanceModel.getGrievanceStatus());
        hashMap.put("message", grievanceModel.getMessage());

        FireBaseHelper.getInstance().updateData(grievanceModel.getCircle(),
                String.valueOf(grievanceModel.getGrievanceType()),
                hashMap,
                FireBaseHelper.ROOT_GRIEVANCES,
                grievanceModel.getIdentifierNumber());
    }

    private void notifyPensioner() {

        FireBaseHelper.getInstance().getDataFromFireBase(null, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fcmKey = (String) dataSnapshot.getValue();
                CustomLogger.getInstance().logDebug("Notification : Key received", CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
                getTokenAndSendNotification(fcmKey);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }, true, FireBaseHelper.ROOT_FCM_KEY);

    }

    private void getTokenAndSendNotification(final String fcmKey) {
        FireBaseHelper.getInstance().getDataFromFireBase(null, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    String token = (String) dataSnapshot.getValue();
                    CustomLogger.getInstance().logDebug("Notification : Token received", CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
                    sendNotification(fcmKey, token);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }, true, FireBaseHelper.ROOT_TOKEN, grievanceModel.getUid());

    }

    private void sendNotification(String fcmKey, String token) {

        FirebaseNotificationHelper.initialize(fcmKey)
                .jsonBody(getJsonBody())
                .receiverFirebaseToken(token)
                .send();

        //        String tag = "Notify";
//        Map<String, String> header = new HashMap<>();
//
//        header.put("Content-Type", "application/json");
//        header.put("Authorization", "Key=" + fcmKey);
//        JSONObject params = new JSONObject();
//        try {
//            params.put("data", getJsonBody());
//            params.put("to", token);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        if (volleyHelper.countRequestsInFlight(tag) == 0)
//            volleyHelper.makeJsonRequest(Constants.FCM_URL, tag, params, header);
    }

    private String getJsonBody() {

        String newStatus = Helper.getInstance().getStatusString(grievanceModel.getGrievanceStatus(), Locale.getDefault());
        String type = grievanceString;
        JSONObject jsonObjectData = new JSONObject();
        try {
            jsonObjectData.put(Constants.KEY_TITLE, getString(R.string.notification_title));
            jsonObjectData.put(Constants.KEY_BODY, String.format(getString(R.string.notification_body), type, newStatus));
            jsonObjectData.put(Constants.KEY_CODE, grievanceModel.getIdentifierNumber());
            jsonObjectData.put(Constants.KEY_GTYPE, grievanceModel.getGrievanceType());
            jsonObjectData.put(Constants.KEY_CIRCLE, grievanceModel.getCircle());
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return jsonObjectData.toString();
    }

    private void onFailure(String message, String title) {
        revertUpdatesOnFireBase();
        progressDialog.dismiss();
        Helper.getInstance().showErrorDialog(message, title, this);
    }

    private void showSuccessDialog() {

        grievanceModel.setGrievanceStatus(status);
        grievanceModel.setMessage(message);
        grievanceModel.setExpanded(true);
        updateState(State.INIT);

        String alertMessage = String.format(getString(R.string.grievance_updation_success),
                grievanceModel.getIdentifierNumber(),
                grievanceString,
                Helper.getInstance().getStatusString(grievanceModel.getGrievanceStatus(), Locale.getDefault()));

        progressDialog.dismiss();
        notifyPensioner();
        setResult(Activity.RESULT_OK);
        Helper.getInstance().showFancyAlertDialog(UpdateGrievanceActivity.this, alertMessage, getString(R.string.success), getString(R.string.ok), () -> {
            finishActivity(RecyclerViewAdapterGrievanceUpdate.REQUEST_UPDATE);
            finish();
        }, null, null, FancyAlertDialogType.SUCCESS);
    }

    private void showImageChooser() {

        imagePicker = Helper.getInstance().showImageChooser(imagePicker, this, true, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {
                CustomLogger.getInstance().logDebug("onPickImage: " + imageUri.getPath(), CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);

            }

            @Override
            public void onCropImage(Uri imageUri) {
                CustomLogger.getInstance().logDebug("onCropImage: " + imageUri.getPath(), CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
                int currentPosition = attachmentModelArrayList.size();
                attachmentModelArrayList.add(currentPosition, new SelectedImageModel(imageUri));
                adapterSelectedImages.notifyItemInserted(currentPosition);
                adapterSelectedImages.notifyDataSetChanged();
                CustomLogger.getInstance().logDebug("onCropImage: Item inserted at " + currentPosition, CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
                setSelectedFileCount(currentPosition + 1);
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
                CustomLogger.getInstance().logDebug("onPermissionDenied: Permission not given to choose textViewMessage", CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
            }
        });

    }

    private void removeAllSelectedImages() {
        if (attachmentModelArrayList == null || adapterSelectedImages == null) {
            return;
        }
        attachmentModelArrayList.clear();
        adapterSelectedImages.notifyDataSetChanged();
        setSelectedFileCount(0);
    }

    public void setSelectedFileCount(int count) {
        textViewAttachedFileCount.setText(String.format(getString(R.string.files_selected), String.valueOf(count)));
    }

    @Override
    public void onMenuItemSelected(View view, int id) {
        switch (items.get(id).getId()) {
            case 0:
                showImageChooser();
                break;
            case 1:
                removeAllSelectedImages();
                break;
        }
    }

    @Override
    public void onResponse(String str) {
        JSONObject jsonObject = Helper.getInstance().getJson(str);
        CustomLogger.getInstance().logDebug(jsonObject.toString(), CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
        try {
            if (jsonObject.get("action").equals("Creating Image")) {
                counterServerImages++;
                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {
                    if (counterServerImages == attachmentModelArrayList.size()) {
                        CustomLogger.getInstance().logDebug("onResponse: Files uploaded", CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
                        updateState(State.UPLOADED_TO_SERVER);
                        doUpdateOnInternetAvailable();
                    }
                } else {
                    onFailure(getString(R.string.file_not_uploaded), getString(R.string.file_upload_error));
                }
            } else if (jsonObject.getString("action").equals("Sending Mail to user")) {
                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {
                    showSuccessDialog();
                } else {
                    onFailure(getString(R.string.grievance_updation_fail), getString(R.string.failure));
                }
            }
        } catch (JSONException jse) {
            jse.printStackTrace();
            onFailure(getString(R.string.grievance_updation_fail), getString(R.string.failure));
        }
    }

    @Override
    public void onError(VolleyError volleyError) {
        volleyError.printStackTrace();
        onFailure(getString(R.string.grievance_updation_fail), getString(R.string.failure));
    }

    @Override
    public void onBackPressed() {
        if (fabMenu != null && fabMenu.isShowing()) {
            fabMenu.closeMenu();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLogger.getInstance().logDebug("onActivityResult: " + requestCode + " ," + resultCode, CustomLogger.Mask.UPDATE_GRIEVANCE_ACTIVITY);
        super.onActivityResult(requestCode, resultCode, data);
        if (imagePicker != null)
            imagePicker.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void updateState(State state) {
        this.state = state;

    }

    //    private void addImageDataToFirebaseDatabase() {
//
//        CustomLogger.getInstance().logDebug( "addImageDataToFireBaseDatabase: ");
//        AtomicInteger uriCounter = new AtomicInteger();
//        CustomLogger.getInstance().logDebug( "addImageDataToFireBaseDatabase: size = " + attachmentModelArrayList.size());
//        for (int i = 0; i < attachmentModelArrayList.size(); i++) {
//
//            Task<Void> task = FireBaseHelper.getInstance(this).uploadDataToFirebase(attachmentModelArrayList.get(i).getImageURI().toString(),
//                    FireBaseHelper.ROOT_IMAGES_BY_STAFF,
//                    grievanceModel.getReferenceNo(),
//                    "Image" + i);
//            CustomLogger.getInstance().logDebug( "addImageDataToFireBaseDatabase: Adding Task");
//            if (task != null) {
//                CustomLogger.getInstance().logDebug( "addImageDataToFireBaseDatabase: task not null");
//                task.addOnFailureListener(exception -> {
//                    CustomLogger.getInstance().logDebug( "addImageDataToFirebaseDatabase: failure");
//                    progressDialog.dismiss();
//                    Helper.getInstance().showErrorDialog("Data could not be uploaded\nTry Again", "Submission Error", this);
//                    CustomLogger.getInstance().logDebug( "onFailure: " + exception.getMessage());
//                })
//                        .addOnSuccessListener(taskSnapshot -> {
//                                    uriCounter.incrementAndGet();
//                                    CustomLogger.getInstance().logDebug( "onSuccess: counter = " + uriCounter + "size = " + attachmentModelArrayList.size());
//                                    if (uriCounter.get() == attachmentModelArrayList.size()) {
//                                        isUploadedToFireBaseDatabase = true;
//                                        doUpdateOnInternetAvailable();
//                                    }
//                                });
//            }
//        }
//    }
}