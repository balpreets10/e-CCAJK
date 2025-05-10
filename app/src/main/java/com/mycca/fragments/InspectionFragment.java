package com.mycca.fragments;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.google.gson.reflect.TypeToken;
import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.adapter.RecyclerViewAdapterSelectedImages;
import com.mycca.custom.CustomProgressButton.CircularProgressButton;
import com.mycca.custom.FabRevealMenu.FabListeners.OnFABMenuSelectedListener;
import com.mycca.custom.FabRevealMenu.FabModel.FABMenuItem;
import com.mycca.custom.FabRevealMenu.FabView.FABRevealMenu;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.MySubmittableFragment;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.custom.customImagePicker.ImagePicker;
import com.mycca.custom.customImagePicker.cropper.CropImage;
import com.mycca.custom.customImagePicker.cropper.CropImageView;
import com.mycca.enums.State;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.models.Circle;
import com.mycca.models.InspectionModel;
import com.mycca.models.SelectedImageModel;
import com.mycca.models.StaffModel;
import com.mycca.providers.CircleDataProvider;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.DataSubmissionAndMail;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.IOHelper;
import com.mycca.tools.MyLocationManager;
import com.mycca.tools.Preferences;
import com.mycca.tools.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mycca.tools.MyLocationManager.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static com.mycca.tools.MyLocationManager.LOCATION_REQUEST_CODE;

public class InspectionFragment extends MySubmittableFragment implements VolleyHelper.VolleyResponse, OnFABMenuSelectedListener {

    private static final String TAG = "Inspection";
    String savedModel;
    boolean isCurrentLocationFound = false;
    // , isUploadedToFirebase = false, isUploadedToServer = false;
    Double latitude, longitude;
    int counterFirebaseImages;
    int counterUpload = 0;
    int counterServerImages = 0;

    AppCompatTextView textViewSelectedFileCount;
    CircularProgressButton circularProgressButton;
    TextView coordinates;
    ProgressBar progressBar;
    EditText editTextLocationName;
    Button upload, save;
    FloatingActionButton fab;
    ImagePicker imagePicker;
    ProgressDialog progressDialog;
    View.OnClickListener getCoordinatesListener;
    RecyclerView recyclerViewSelectedImages;
    RecyclerViewAdapterSelectedImages adapterSelectedImages;

    MainActivity mainActivity;
    MyLocationManager myLocationManager;
    LocationCallback mLocationCallback;
    VolleyHelper volleyHelper;
    StaffModel staffModel;
    InspectionModel inspectionModel;

    ArrayList<SelectedImageModel> selectedImageModelArrayList;
    ArrayList<Uri> firebaseImageURLs;
    Uri downloadUrl;
    private ArrayList<FABMenuItem> items;

    public InspectionFragment() {

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inspection, container, false);
        if (getArguments() != null) {
            savedModel = getArguments().getString("SavedModel", null);
        }
        bindViews(view);
        init(view);
//        if (Preferences.getInstance().getBooleanPref(getContext(), Preferences.PREF_HELP_INSPECTION)) {
//            showTutorial();
//            Preferences.getInstance().setBooleanPref(getContext(), Preferences.PREF_HELP_INSPECTION, false);
//        } else
        if (savedModel == null)
            getLocationCoordinates();
        else
            coordinates.setOnClickListener(null);
        return view;
    }

    private void bindViews(View view) {

        fab = view.findViewById(R.id.fab_inspection);
        editTextLocationName = view.findViewById(R.id.edittext_current_location_name);
        coordinates = view.findViewById(R.id.textview_current_location_coordinates);
        progressBar = view.findViewById(R.id.progressBar);
        recyclerViewSelectedImages = view.findViewById(R.id.recycler_view_selected_images_inspection);
        upload = view.findViewById(R.id.button_upload);
        save = view.findViewById(R.id.button_save_inspection);
        textViewSelectedFileCount = view.findViewById(R.id.textview_selected_image_count_inspection);
    }

    private void init(View view) {
        mainActivity = (MainActivity) getActivity();
        initItems();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    showCoordinates(location.getLatitude(), location.getLongitude());
                }
            }
        };

        myLocationManager = new MyLocationManager(this, mLocationCallback);
        volleyHelper = new VolleyHelper(this, getContext());
        progressDialog = Helper.getInstance().getProgressWindow(mainActivity, getString(R.string.please_wait));

        getCoordinatesListener = v -> getLocationCoordinates();

        coordinates.setOnClickListener(getCoordinatesListener);
        coordinates.setText(R.string.location_not_found);
        //circularProgressButton.setIndeterminateProgressMode(true);
        //circularProgressButton.setOnClickListener(getCoordinatesListener);
        //circularProgressButton.setIdleText(getString(R.string.location_not_found));

        final FABRevealMenu fabMenu = view.findViewById(R.id.fabMenu_inspection);
        try {
            if (fab != null && fabMenu != null) {
                mainActivity.setFabRevealMenu(fabMenu);
                fabMenu.setMenuItems(items);
                fabMenu.bindAnchorView(fab);
                fabMenu.setOnFABMenuSelectedListener(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        upload.setOnClickListener(v -> {
            if (checkInput())
                doSubmission();
        });

        save.setOnClickListener(v -> {
            if (checkInput())
                saveInspection();
        });

        selectedImageModelArrayList = new ArrayList<>();
        if (savedModel != null) {
            InspectionModel model = (InspectionModel) Helper.getInstance().getObjectFromJson(savedModel, InspectionModel.class);
            showCoordinates(model.getLatitude(), model.getLongitude());
            editTextLocationName.setText(model.getLocationName());
            selectedImageModelArrayList = (ArrayList<SelectedImageModel>) Helper.getInstance().getImagesFromString(model.getFilePathList());
            if (selectedImageModelArrayList != null)
                setSelectedFileCount(selectedImageModelArrayList.size());
            else
                selectedImageModelArrayList = new ArrayList<>();
        }

        adapterSelectedImages = new RecyclerViewAdapterSelectedImages(selectedImageModelArrayList, this);
        recyclerViewSelectedImages.setAdapter(adapterSelectedImages);
        recyclerViewSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

    }

    private void initItems() {
        items = new ArrayList<>();
        items.add(new FABMenuItem(0, getString(R.string.add_image), AppCompatResources.getDrawable(mainActivity, R.drawable.ic_attach_file_white_24dp)));
        items.add(new FABMenuItem(1, getString(R.string.remove_all), AppCompatResources.getDrawable(mainActivity, R.drawable.ic_close_24dp)));
    }

    private void showCoordinates(double latitude, double longitude) {
        isCurrentLocationFound = true;
        this.latitude = latitude;
        this.longitude = longitude;
        myLocationManager.cleanUp();

        CustomLogger.getInstance().logDebug("getLocationCoordinates: " + latitude + "," + longitude, CustomLogger.Mask.INSPECTION_FRAGMENT);
        //circularProgressButton.setProgress(0);
        //circularProgressButton.setIdleText(String.format(getString(R.string.current_location), String.valueOf(latitude), String.valueOf(longitude)));
        manageVisibility(true);
        coordinates.setText(String.format(getString(R.string.current_location), String.valueOf(latitude), String.valueOf(longitude)));
    }

    private void manageVisibility(boolean showTextview) {
        if (showTextview) {
            coordinates.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else {
            coordinates.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void getLocationCoordinates() {
        latitude = null;
        longitude = null;
        manageVisibility(false);//circularProgressButton.setProgress(1);
        getLocation();
    }

    private void getLocation() {
        Task<LocationSettingsResponse> task = myLocationManager.ManageLocation();
        if (task != null) {
            task.addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    CustomLogger.getInstance().logDebug("Task is Successful\nRequesting Location Update", CustomLogger.Mask.INSPECTION_FRAGMENT);
                    myLocationManager.requestLocationUpdates();

                } else {
                    CustomLogger.getInstance().logDebug("Task UnSuccessful", CustomLogger.Mask.INSPECTION_FRAGMENT);
                    manageVisibility(true);
                    //circularProgressButton.setProgress(0);
                }
            });
            task.addOnSuccessListener(mainActivity, locationSettingsResponse -> CustomLogger.getInstance().logDebug("On Task Success", CustomLogger.Mask.INSPECTION_FRAGMENT));

            task.addOnFailureListener(mainActivity, e -> {
                CustomLogger.getInstance().logDebug("On Task Failed", CustomLogger.Mask.INSPECTION_FRAGMENT);
                // circularProgressButton.setProgress(0);
                // circularProgressButton.setIdleText(getString(R.string.location_not_found));
                if (e instanceof ResolvableApiException) {
                    myLocationManager.onLocationAcccessRequestFailure(e);
                }
            });
        }
    }

    private boolean checkInput() {
        if (editTextLocationName.getText().toString().trim().isEmpty()) {
            editTextLocationName.setError(getString(R.string.location_req));
            return false;
        } else if (!isCurrentLocationFound) {
            Toast.makeText(getContext(), getString(R.string.coordinates_req), Toast.LENGTH_LONG).show();
            return false;
        } else if (selectedImageModelArrayList.size() == 0) {
            Toast.makeText(getContext(), getString(R.string.image_req), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void saveInspection() {
        String fileList;
        if (selectedImageModelArrayList != null)
            fileList = Helper.getInstance().getStringFromList(selectedImageModelArrayList);
        else
            fileList = null;
        CustomLogger.getInstance().logDebug(fileList, CustomLogger.Mask.INSPECTION_FRAGMENT);
        InspectionModel model = new InspectionModel(editTextLocationName.getText().toString(),
                fileList, latitude, longitude, new Date());
        CustomLogger.getInstance().logDebug("Saving inspection data: " + model.getLocationName() + "," + model.getLatitude() + "," +
                +model.getLongitude() + "," + model.getDate(), CustomLogger.Mask.INSPECTION_FRAGMENT);
        Type collectionType = new TypeToken<ArrayList<InspectionModel>>() {
        }.getType();
        Helper.getInstance().saveModelOffline(mainActivity, model, collectionType, IOHelper.INSPECTIONS);

    }

    private void doSubmission() {

        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                CustomLogger.getInstance().logDebug("version checked =" + Helper.versionChecked, CustomLogger.Mask.INSPECTION_FRAGMENT);
                if (Helper.versionChecked) {
                    doSubmissionOnInternetAvailable();
                } else {
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                long value = (long) dataSnapshot.getValue();
                                if (Helper.getInstance().onLatestVersion(value, mainActivity))
                                    doSubmissionOnInternetAvailable();
                            } catch (Exception e) {
                                Helper.getInstance().showMaintenanceDialog(mainActivity, null);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Helper.getInstance().showMaintenanceDialog(mainActivity, null);
                        }
                    };
                    FireBaseHelper.getInstance().getDataFromFireBase(null, valueEventListener, true, FireBaseHelper.ROOT_INITIAL_CHECKS, FireBaseHelper.ROOT_APP_VERSION);
                }

            }

            @Override
            public void OnConnectionNotAvailable() {
                progressDialog.dismiss();
                Helper.getInstance().noInternetDialog(mainActivity);
            }
        });
        connectionUtility.checkConnectionAvailability();

    }

    private void doSubmissionOnInternetAvailable() {
//        CustomLogger.getInstance().logDebug("doSubmissionOnInternetAvailable: \n Firebase = " + isUploadedToFirebase + "\n" +
//                "Server = " + isUploadedToServer, CustomLogger.Mask.INSPECTION_FRAGMENT);
//        if (isUploadedToFirebase) {
//            if (isUploadedToServer) {
//                sendFinalMail();
//            } else {
//                uploadImagesToServer();
//            }
//        } else {
//            uploadInspectionDataToFirebase();
//        }

        switch (state) {
            case INIT: {
                uploadInspectionDataToFirebase();
                break;
            }
            case OTP_VERIFIED: {
//                uploadInspectionDataToFirebase();
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
    }

    private void uploadInspectionDataToFirebase() {

        Date date = new Date();
        String locName = editTextLocationName.getText().toString().trim();
        final String key = locName.replaceAll("\\s", "-") + "-" +
                Helper.getInstance().formatDate(date, Helper.DateFormat.DD_MM_YYYY);

        staffModel = Preferences.getInstance().getStaffPref(getContext());
        inspectionModel = new InspectionModel(staffModel.getId(),
                staffModel.getEmail(), locName, latitude, longitude, new Date());

        Task<Void> task = FireBaseHelper.getInstance().uploadDataToFireBase(staffModel.getCircle(),
                inspectionModel,
                FireBaseHelper.ROOT_INSPECTION,
                key);
        task.addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                uploadInspectionFiles(key);
            }
        });
        task.addOnFailureListener(e -> {
            progressDialog.dismiss();
            Helper.getInstance().showMaintenanceDialog(mainActivity, staffModel.getCircle());
        });
    }

    private void uploadInspectionFiles(String key) {
        firebaseImageURLs = new ArrayList<>();
        UploadTask uploadTask;
        counterFirebaseImages = 0;
        counterUpload = 0;
        for (SelectedImageModel imageModel : selectedImageModelArrayList) {
            uploadTask = FireBaseHelper.getInstance().uploadFiles(staffModel.getCircle(),
                    imageModel,
                    true,
                    counterFirebaseImages++,
                    FireBaseHelper.ROOT_INSPECTION,
                    key);

            if (uploadTask != null) {
                uploadTask.addOnFailureListener(exception -> {
                    showError(getString(R.string.file_not_uploaded), getString(R.string.file_upload_error));
                    CustomLogger.getInstance().logDebug("onFailure: " + exception.getMessage(), CustomLogger.Mask.INSPECTION_FRAGMENT);

                }).addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    downloadUrl = uri;
                    firebaseImageURLs.add(downloadUrl);
                    progressDialog.setMessage(String.format(getString(R.string.uploaded_file), String.valueOf(++counterUpload), String.valueOf(selectedImageModelArrayList.size())));
                    if (counterUpload == selectedImageModelArrayList.size()) {
                        updateState(State.UPLOADED_TO_FIREBASE);
//                        isUploadedToFirebase = true;
                        doSubmission();
                    }
                }));
            }
        }
    }

    private void showError(String message, String title) {
        progressDialog.dismiss();
        Helper.getInstance().showErrorDialog(message, title, mainActivity);

    }

    private void uploadImagesToServer() {

        counterServerImages = 0;
        progressDialog.setMessage(getString(R.string.processing));
        int totalFilesToAttach = selectedImageModelArrayList.size();
        if (totalFilesToAttach != 0) {
            DataSubmissionAndMail.getInstance().uploadImagesToServer(firebaseImageURLs,
                    editTextLocationName.getText().toString(),
                    DataSubmissionAndMail.SUBMIT,
                    volleyHelper);
        } else {
            updateState(State.UPLOADED_TO_SERVER);
//            isUploadedToServer = true;
            doSubmission();
        }
    }

    private void sendFinalMail() {
        progressDialog.setMessage(getString(R.string.almost_done));
        String url = Helper.getInstance().getAPIUrl() + "sendInspectionEmail.php/";
        Circle circle = CircleDataProvider.getInstance().getCircleFromCode(staffModel.getCircle());
        Map<String, String> params = new HashMap<>();

        //params.put("mailTo",circle.getMails());
        //params.put("mailFrom",staffModel.getEmail());
        params.put("locationName", inspectionModel.getLocationName());
        params.put("staffID", staffModel.getId());
        params.put("folder", DataSubmissionAndMail.SUBMIT);
        params.put("location", latitude + "," + longitude);
        params.put("fileCount", selectedImageModelArrayList.size() + "");

        DataSubmissionAndMail.getInstance().sendMail(params, "send_inspection_mail-" + staffModel.getId(), volleyHelper, url);
    }

    public void setSelectedFileCount(int count) {
        textViewSelectedFileCount.setText(String.format(getString(R.string.files_selected), String.valueOf(count)));
    }

    private void showImageChooser() {
        imagePicker = Helper.getInstance().showImageChooser(imagePicker, mainActivity, true, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {
                CustomLogger.getInstance().logDebug("onPickImage: " + imageUri.getPath(), CustomLogger.Mask.INSPECTION_FRAGMENT);
            }

            @Override
            public void onCropImage(Uri imageUri) {
                CustomLogger.getInstance().logDebug("onCropImage: " + imageUri.getPath());
                int currentPosition = selectedImageModelArrayList.size();
                selectedImageModelArrayList.add(currentPosition, new SelectedImageModel(imageUri));
                adapterSelectedImages.notifyItemInserted(currentPosition);
                adapterSelectedImages.notifyDataSetChanged();
                setSelectedFileCount(currentPosition + 1);
                CustomLogger.getInstance().logDebug("onCropImage: Item inserted at " + currentPosition, CustomLogger.Mask.INSPECTION_FRAGMENT);
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
                CustomLogger.getInstance().logDebug("onPermissionDenied: Permission not given to choose textViewMessage", CustomLogger.Mask.INSPECTION_FRAGMENT);
            }
        });

    }

    private void removeAllSelectedImages() {
        if (selectedImageModelArrayList == null || adapterSelectedImages == null) {
            return;
        }
        selectedImageModelArrayList.clear();
        adapterSelectedImages.notifyDataSetChanged();
        textViewSelectedFileCount.setText(getResources().getString(R.string.no_image));
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
    public void onError(VolleyError volleyError) {
        volleyError.printStackTrace();
        showError(getString(R.string.try_again), getString(R.string.some_error));
    }

    @Override
    public void onResponse(String str) {
        JSONObject jsonObject = Helper.getInstance().getJson(str);
        CustomLogger.getInstance().logDebug(jsonObject.toString(), CustomLogger.Mask.INSPECTION_FRAGMENT);
        try {
            if (jsonObject.get("action").equals("Creating Image")) {
                counterServerImages++;
                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {
                    if (counterServerImages == selectedImageModelArrayList.size()) {
                        CustomLogger.getInstance().logDebug("onResponse: Files uploaded", CustomLogger.Mask.INSPECTION_FRAGMENT);
                        updateState(State.UPLOADED_TO_SERVER);
//                        isUploadedToServer = true;
                        doSubmission();
                    }
                } else {
                    CustomLogger.getInstance().logDebug("onResponse: Image = " + counterServerImages + " failed", CustomLogger.Mask.INSPECTION_FRAGMENT);
                    showError(getString(R.string.file_not_uploaded), getString(R.string.file_upload_error));
                }
            } else if (jsonObject.getString("action").equals("Sending Mail")) {

                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {

                    progressDialog.dismiss();
                    String alertMessage = String.format(getString(R.string.inspection_success),
                            inspectionModel.getLocationName());
                    Helper.getInstance().showMessage(mainActivity, alertMessage, getString(R.string.success),
                            FancyAlertDialogType.SUCCESS);
                    updateState(State.INIT);
//                    isUploadedToServer = isUploadedToFirebase = false;

                } else {
                    showError(getString(R.string.inspection_failed), getString(R.string.failure));
                }
            }
        } catch (JSONException jse) {
            jse.printStackTrace();
            showError(getString(R.string.try_again), getString(R.string.some_error));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLogger.getInstance().logDebug("onActivityResult: " + requestCode + " ," + resultCode, CustomLogger.Mask.INSPECTION_FRAGMENT);
        super.onActivityResult(requestCode, resultCode, data);
        if (imagePicker != null)
            imagePicker.onActivityResult(mainActivity, requestCode, resultCode, data);

        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST: {
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        CustomLogger.getInstance().logDebug("Resolution success", CustomLogger.Mask.INSPECTION_FRAGMENT);
                        myLocationManager.requestLocationUpdates();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        CustomLogger.getInstance().logDebug("Resolution denied", CustomLogger.Mask.INSPECTION_FRAGMENT);
                        myLocationManager.ShowDialogOnLocationOff(getString(R.string.msg_no_location));
                        break;
                    }
                    default: {
                        CustomLogger.getInstance().logDebug("User unable to do anything", CustomLogger.Mask.INSPECTION_FRAGMENT);
                        break;
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CustomLogger.getInstance().logDebug("onRequestPermissionsResult: " + "Inspection", CustomLogger.Mask.INSPECTION_FRAGMENT);

        switch (requestCode) {

            case LOCATION_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocationCoordinates();

                } else {
                    myLocationManager.ShowDialogOnPermissionDenied(getString(R.string.msg_no_location));
                }
                break;
            }
            default: {
                if (imagePicker != null)
                    imagePicker.onRequestPermissionsResult(mainActivity, requestCode, permissions, grantResults);
            }

        }

    }


    @Override
    public void updateState(State state) {
        this.state = state;
    }
}


//    private void showTutorial() {
//
//        final FancyShowCaseView fancyShowCaseView1 = new FancyShowCaseView.Builder(mainActivity)
//                .title("Click to refresh location")
//                .focusOn(circularProgressButton)
//                .focusShape(FocusShape.ROUNDED_RECTANGLE)
//                .build();
//
//        mainActivity.setmQueue(new FancyShowCaseQueue()
//                .add(fancyShowCaseView1));
//
//        mainActivity.getmQueue().setCompleteListener(() -> {
//            mainActivity.setmQueue(null);
//            getLocationCoordinates();
//        });
//
//        mainActivity.getmQueue().show();
//    }
