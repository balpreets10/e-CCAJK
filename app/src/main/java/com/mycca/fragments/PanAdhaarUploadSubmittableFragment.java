package com.mycca.fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.content.res.AppCompatResources;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.activity.VerificationActivity;
import com.mycca.adapter.GenericSpinnerAdapter;
import com.mycca.custom.FabRevealMenu.FabListeners.OnFABMenuSelectedListener;
import com.mycca.custom.FabRevealMenu.FabModel.FABMenuItem;
import com.mycca.custom.FabRevealMenu.FabView.FABRevealMenu;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.MySubmittableFragment;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.custom.barCode.BarcodeCaptureActivity;
import com.mycca.custom.customImagePicker.ImagePicker;
import com.mycca.custom.customImagePicker.cropper.CropImage;
import com.mycca.custom.customImagePicker.cropper.CropImageView;
import com.mycca.enums.State;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.models.Circle;
import com.mycca.models.PanAdhaar;
import com.mycca.models.SelectedImageModel;
import com.mycca.providers.CircleDataProvider;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.DataSubmissionAndMail;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.Preferences;
import com.mycca.tools.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


public class PanAdhaarUploadSubmittableFragment extends MySubmittableFragment implements VolleyHelper.VolleyResponse, OnFABMenuSelectedListener {

    private static final int RC_BARCODE = 9004, REQUEST_OTP = 8543;
    ImageView imageviewSelectedImage;
    TextView textViewFileName;
    Spinner spinnerCircle;
    TextInputLayout textInputLayoutIdentifier, textInputLayoutCardNumber;
    TextInputEditText editTextIdentifier, editTextCardNumber, editTextMobileNumber;
    Button buttonUpload;
    FloatingActionButton fab;
    RadioGroup radioGroup;
    LinearLayout linearLayout;
    ProgressDialog progressDialog;

    private static final String TAG = "PanAdhaarUpload";
    public static final String AADHAAR_DATA_TAG = "PrintLetterBarcodeData", AADHAR_UID_ATTR = "uid";
    private String pensionerIdentifier, cardNumber, mobile, root;
    private int field2Hint, identifierHint;
    //    private boolean isUploadedToFirebase = false, isUploadedToServer = false, isOTPVerified = false;
    private ArrayList<FABMenuItem> items;
    SelectedImageModel imageModel;
    ImagePicker imagePicker;
    ArrayList<Uri> firebaseImageURLs;
    Uri downloadUrl;
    VolleyHelper volleyHelper;
    Circle circle;
    MainActivity mainActivity;
    private static final int MY_CAMERA_REQUEST_CODE = 300;


    public PanAdhaarUploadSubmittableFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adhaar_pan_upload, container, false);
        if (this.getArguments() != null)
            root = this.getArguments().getString("Root");
        bindViews(view);
        init(view);
        return view;
    }

    private void bindViews(View view) {
        linearLayout = view.findViewById(R.id.layout_radio_group);
        radioGroup = view.findViewById(R.id.radio_group_identifier_type);
        textInputLayoutIdentifier = view.findViewById(R.id.text_input_pensioner_code);
        textInputLayoutCardNumber = view.findViewById(R.id.text_number);
        editTextIdentifier = view.findViewById(R.id.et_pan_adhaar_pcode);
        editTextCardNumber = view.findViewById(R.id.et_pan_adhaar_number);
        editTextMobileNumber = view.findViewById(R.id.et_pan_adhaar_mobile);
        spinnerCircle = view.findViewById(R.id.spinner_pan_adhaar_circle);
        textViewFileName = view.findViewById(R.id.textview_filename);
        imageviewSelectedImage = view.findViewById(R.id.imageview_selected_image);
        fab = view.findViewById(R.id.fab_aadhar_pan);
        buttonUpload = view.findViewById(R.id.button_upload);
    }

    private void init(View view) {
        mainActivity = (MainActivity) getActivity();
        progressDialog = new ProgressDialog(mainActivity != null ? mainActivity : getActivity());
        identifierHint = R.string.p_code;
        volleyHelper = new VolleyHelper(this, getContext());
        initItems();

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonPensioner:
                    identifierHint = R.string.p_code;
                    editTextIdentifier.setFilters(Helper.getInstance().limitInputLength(15));
                    break;

                case R.id.radioButtonHR:
                    identifierHint = R.string.hr_num;
                    editTextIdentifier.setFilters(new InputFilter[]{});
                    break;
            }
            editTextIdentifier.setText("");
            editTextIdentifier.setError(null);
            textInputLayoutIdentifier.setHint(getString(identifierHint));
        });

        switch (root) {
            case FireBaseHelper.ROOT_ADHAAR:
                editTextCardNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                editTextCardNumber.setFilters(Helper.getInstance().limitInputLength(12));
                field2Hint = R.string.aadhaar_no;
                break;

            case FireBaseHelper.ROOT_PAN:
                editTextCardNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10), (source, start, end, dest, dstart, dend) -> {
                    if (source.equals("")) { // for backspace
                        return source;
                    }
                    if (source.toString().matches("[a-zA-Z0-9]+")) {
                        return source;
                    }
                    return "";
                }});
                field2Hint = R.string.pan_no;
                break;

            default:
                linearLayout.setVisibility(View.GONE);
                field2Hint = R.string.applicant_name;
                break;

        }
        textInputLayoutCardNumber.setHint(getString(field2Hint));
        editTextIdentifier.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_black_24dp, 0, 0, 0);
        editTextCardNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_card_black_24dp, 0, 0, 0);
        editTextMobileNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone_android_black_24dp, 0, 0, 0);

        final FABRevealMenu fabMenu = view.findViewById(R.id.fabMenu_pan_aadhar);
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

        firebaseImageURLs = new ArrayList<>();

        GenericSpinnerAdapter<Circle> statesAdapter = new GenericSpinnerAdapter<>(getContext(),
                CircleDataProvider.getInstance().getActiveCircleData());
        spinnerCircle.setAdapter(statesAdapter);

        buttonUpload.setOnClickListener(v -> {
            if (state == State.UPLOADED_TO_FIREBASE || state == State.UPLOADED_TO_SERVER)
//            if (isUploadedToFirebase || isUploadedToServer)
                doSubmission();
            else if (checkInputBeforeSubmission())
                showConfirmSubmissionDialog();
        });
        updateState(State.INIT);
    }

    private void initItems() {
        items = new ArrayList<>();
        items.add(new FABMenuItem(0, getString(R.string.add_image), AppCompatResources.getDrawable(mainActivity, R.drawable.ic_attach_file_white_24dp)));
        items.add(new FABMenuItem(1, getString(R.string.remove_image), AppCompatResources.getDrawable(mainActivity, R.drawable.ic_close_24dp)));
        if (root.equals(FireBaseHelper.ROOT_ADHAAR))
            items.add(new FABMenuItem(2, getString(R.string.scan_adhaar), AppCompatResources.getDrawable(mainActivity, R.drawable.aadhaar_logo)));

    }

    private boolean checkInputBeforeSubmission() {
        pensionerIdentifier = editTextIdentifier.getText().toString();
        cardNumber = editTextCardNumber.getText().toString();
        circle = (Circle) spinnerCircle.getSelectedItem();
        mobile = editTextMobileNumber.getText().toString();

        //If Pensioner code is empty
        if (pensionerIdentifier.trim().length() != 15 && identifierHint == R.string.p_code) {
            editTextIdentifier.setError(getString(R.string.invalid_p_code));
            editTextIdentifier.requestFocus();
            return false;
        } else if (pensionerIdentifier.trim().isEmpty() && identifierHint == R.string.hr_num) {
            editTextIdentifier.setError(getString(R.string.invalid_hr_num));
            editTextIdentifier.requestFocus();
            return false;
        }
        //If Aadhar Number is not complete
        else if ((root.equals(FireBaseHelper.ROOT_ADHAAR)) && (cardNumber.length() != 12)) {
            editTextCardNumber.setError(getString(R.string.invalid_aadhaar));
            editTextCardNumber.requestFocus();
            return false;
        }
        //If PAN Number is not complete
        else if ((root.equals(FireBaseHelper.ROOT_PAN)) && (cardNumber.length() != 10)) {
            editTextCardNumber.setError(getString(R.string.invalid_pan));
            editTextCardNumber.requestFocus();
            return false;
        } else if (cardNumber.isEmpty()) {
            editTextCardNumber.setError(getString(R.string.invalid_name));
            editTextCardNumber.requestFocus();
            return false;
        } else if (mobile.length() < 10) {
            editTextMobileNumber.setError(getString(R.string.invalid_mobile));
            editTextMobileNumber.requestFocus();
            return false;
        }
        //if no file selected
        else if (imageModel == null) {
            textViewFileName.setError("");
            textViewFileName.setText(getResources().getString(R.string.no_image));
            return false;
        }
        return true;
    }

    private void showConfirmSubmissionDialog() {
        Helper.getInstance().hideKeyboardFrom(getActivity());
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_confirm_submission, (ViewGroup) this.getView(), false);
        loadValues(v);
        Helper.getInstance().getConfirmationDialog(getActivity(), v,
                (dialog, which) -> doSubmission());
    }

    private void loadValues(View v) {

        TextView pensionerHeading = v.findViewById(R.id.textview_confirm1);
        TextView pensionerValue = v.findViewById(R.id.textview_confirm1_value);
        TextView mobileNum = v.findViewById(R.id.textview_confirm2_value);
        TextView heading = v.findViewById(R.id.textview_confirm3);
        TextView value = v.findViewById(R.id.textview_confirm3_value);
        TextView circle = v.findViewById(R.id.textview_confirm4_value);

        pensionerHeading.setText(getString(identifierHint));
        pensionerValue.setText(pensionerIdentifier);
        mobileNum.setText(mobile);
        heading.setText(getString(field2Hint));
        value.setText(cardNumber);
        circle.setText(Preferences.getInstance().getStringPref(mainActivity, Preferences.PREF_LANGUAGE)
                .equals("hi") ? this.circle.getHi() : this.circle.getEn());

        v.findViewById(R.id.textview_confirm5).setVisibility(View.GONE);
        v.findViewById(R.id.textview_confirm5_value).setVisibility(View.GONE);
        v.findViewById(R.id.textview_confirm6).setVisibility(View.GONE);
        v.findViewById(R.id.textview_confirm6_value).setVisibility(View.GONE);
        v.findViewById(R.id.textview_confirm7).setVisibility(View.GONE);
        v.findViewById(R.id.textview_confirm7_value).setVisibility(View.GONE);
    }

    private void doSubmission() {
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                CustomLogger.getInstance().logDebug("version checked = " + Helper.versionChecked, CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
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
                            Helper.getInstance().showMaintenanceDialog(getActivity(), null);
                        }
                    };
                    FireBaseHelper.getInstance().getDataFromFireBase(null,
                            valueEventListener, true, FireBaseHelper.ROOT_INITIAL_CHECKS, FireBaseHelper.ROOT_APP_VERSION);
                }

            }

            @Override
            public void OnConnectionNotAvailable() {
                Helper.getInstance().noInternetDialog(mainActivity);
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    private void doSubmissionOnInternetAvailable() {
        switch (state) {
            case INIT: {
                otp();
                break;
            }
            case OTP_VERIFIED: {
                uploadDataToFirebase();
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
//        if (isOTPVerified) {
//            if (isUploadedToFirebase) {
//                if (isUploadedToServer) {
//                    sendFinalMail();
//                } else {
//                    uploadImagesToServer();
//                }
//            } else {
//                uploadDataToFirebase();
//            }
//        } else
//            otp();
    }

    public void otp() {
        Intent intent = new Intent(mainActivity, VerificationActivity.class)
                .putExtra(VerificationActivity.INTENT_PHONENUMBER, mobile)
                .putExtra(VerificationActivity.INTENT_COUNTRY_CODE, "91");

        mainActivity.startActivityForResult(intent, REQUEST_OTP);

    }

    private void uploadDataToFirebase() {
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        PanAdhaar panAadharModel = new PanAdhaar(getString(identifierHint), pensionerIdentifier, cardNumber);

        Task<Void> task = FireBaseHelper.getInstance().uploadDataToFireBase(circle.getCode(), panAadharModel,
                root,
                pensionerIdentifier);
        task.addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                uploadAllImagesToFirebase();
            } else {
                progressDialog.dismiss();
                Helper.getInstance().showMaintenanceDialog(getActivity(), circle.getCode());
            }
        });
    }

    private void uploadAllImagesToFirebase() {
        UploadTask uploadTask = FireBaseHelper.getInstance().uploadFiles(circle.getCode(),
                imageModel,
                false,
                0,
                root,
                pensionerIdentifier);

        if (uploadTask != null) {
            uploadTask.addOnFailureListener(exception -> {
                showError(getString(R.string.file_upload_error), getString(R.string.file_not_uploaded));
                CustomLogger.getInstance().logDebug("onFailure: " + exception.getMessage(), CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
            }).addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                downloadUrl = uri;
                CustomLogger.getInstance().logDebug("onSuccess: " + downloadUrl, CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
                firebaseImageURLs.add(downloadUrl);
                updateState(State.UPLOADED_TO_FIREBASE);
//                isUploadedToFirebase = true;
                doSubmission();
            }));
        }
    }

    public void showError(String title, String message) {
        progressDialog.dismiss();
        Helper.getInstance().showErrorDialog(message, title, mainActivity);
    }

    private void uploadImagesToServer() {

        progressDialog.setMessage(getString(R.string.processing));
        DataSubmissionAndMail.getInstance().uploadImagesToServer(firebaseImageURLs,
                pensionerIdentifier,
                DataSubmissionAndMail.SUBMIT,
                volleyHelper);

    }

    private void sendFinalMail() {

        progressDialog.setMessage(getString(R.string.almost_done));
        String url = Helper.getInstance().getAPIUrl() + "sendInfoUpdateEmail.php/";
        Map<String, String> params = new HashMap<>();

        String hint1_en = Helper.getInstance().getEnglishString(identifierHint);
        String hint2_en = Helper.getInstance().getEnglishString(field2Hint);
        CustomLogger.getInstance().logDebug(hint1_en + ", " + hint2_en);

        //params.put("mailTo",circle.getMails());
        params.put("pensionerIdentifier", pensionerIdentifier);
        params.put("folder", DataSubmissionAndMail.SUBMIT);
        params.put("personType", hint1_en);
        params.put("updateType", root);
        params.put("fieldName", hint2_en);
        params.put("value", cardNumber);

        DataSubmissionAndMail.getInstance().sendMail(params, "send_mail-" + pensionerIdentifier, volleyHelper, url);
    }

    public void scanNow() {

        CustomLogger.getInstance().logDebug("scanNow: ", CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
        Intent intent = new Intent(mainActivity, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
        mainActivity.startActivityForResult(intent, RC_BARCODE);
    }

    protected void processScannedData(String scanData) {
        CustomLogger.getInstance().logDebug(scanData, CustomLogger.Mask.PAN_AADHAR_FRAGMENT);

        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(new StringReader(scanData));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    CustomLogger.getInstance().logDebug("AadharPan Start document", CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
                } else if (eventType == XmlPullParser.START_TAG && AADHAAR_DATA_TAG.equals(parser.getName())) {
                    // extract data from tag
                    String uid = parser.getAttributeValue(null, AADHAR_UID_ATTR);
                    editTextCardNumber.setText(uid);

                } else if (eventType == XmlPullParser.END_TAG) {
                    CustomLogger.getInstance().logDebug("AadharPan End tag " + parser.getName(), CustomLogger.Mask.PAN_AADHAR_FRAGMENT);

                } else if (eventType == XmlPullParser.TEXT) {
                    CustomLogger.getInstance().logDebug("AadharPan Text " + parser.getText(), CustomLogger.Mask.PAN_AADHAR_FRAGMENT);

                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

    }

    private void showImageChooser() {
        imagePicker = Helper.getInstance().showImageChooser(imagePicker, getActivity(), true, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {

            }

            @Override
            public void onCropImage(Uri imageUri) {
                Glide.with(mainActivity).load(imageUri).into(imageviewSelectedImage);
                imageModel = new SelectedImageModel(imageUri);
                textViewFileName.setError(null);
                String text = root + ".jpg";
                textViewFileName.setText(text);
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
                CustomLogger.getInstance().logDebug("onPermissionDenied: Permission not given to choose textViewMessage", CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
            }
        });
    }

    @Override
    public void onMenuItemSelected(View view, int id) {
        switch (items.get(id).getId()) {
            case 0:
                showImageChooser();
                break;
            case 1:
                imageviewSelectedImage.setImageResource(0);
                textViewFileName.setText(getResources().getString(R.string.no_image));
                break;
            case 2:
                scanNow();
                break;
        }
    }

    @Override
    public void onResponse(String str) {
        JSONObject jsonObject = Helper.getInstance().getJson(str);
        CustomLogger.getInstance().logDebug(jsonObject.toString(), CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
        try {
            if (jsonObject.get("action").equals("Creating Image")) {
                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {
                    CustomLogger.getInstance().logDebug("onResponse: Files uploaded", CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
                    updateState(State.UPLOADED_TO_SERVER);
//                    isUploadedToServer = true;
                    doSubmission();
                } else {
                    CustomLogger.getInstance().logDebug("onResponse: Image upload failed", CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
                    showError(getString(R.string.file_upload_error), getString(R.string.file_not_uploaded));
                }
            } else if (jsonObject.getString("action").equals("Sending Mail")) {
                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {
                    progressDialog.dismiss();
                    updateState(State.INIT);
//                    isUploadedToServer = isUploadedToFirebase = false;
                    Helper.getInstance().showMessage(getActivity(),
                            String.format(getString(R.string.updation_success), pensionerIdentifier),
                            getString(R.string.success),
                            FancyAlertDialogType.SUCCESS);

                } else {
                    showError(getString(R.string.failure),
                            getString(R.string.updation_failed));
                }
            }
        } catch (JSONException jse) {
            jse.printStackTrace();
            showError(getString(R.string.some_error), getString(R.string.try_again));
        }
    }

    @Override
    public void onError(VolleyError volleyError) {
        showError(getString(R.string.some_error), getString(R.string.try_again));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLogger.getInstance().logDebug("onActivityResult: " + requestCode + " ," + resultCode, CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_BARCODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    CustomLogger.getInstance().logDebug("Barcode read: " + barcode.displayValue, CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
                    processScannedData(barcode.displayValue);
                } else {
                    CustomLogger.getInstance().logDebug("No barcode captured, intent data is null", CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
                }
            } else {
                CustomLogger.getInstance().logDebug(String.format(getString(R.string.barcode_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)), CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
            }
        }
        if (requestCode == REQUEST_OTP) {
            if (resultCode == RESULT_OK) {
                CustomLogger.getInstance().logDebug("Verification complete", CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
                updateState(State.OTP_VERIFIED);
//                isOTPVerified = true;
                doSubmissionOnInternetAvailable();
            } else {
                Helper.getInstance().showErrorDialog(getString(R.string.try_again), getString(R.string.failed), mainActivity);
            }

        } else if (imagePicker != null)
            imagePicker.onActivityResult(this.getActivity(), requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CustomLogger.getInstance().logDebug("onRequestPermissionsResult: " + "PanAadhar " + requestCode, CustomLogger.Mask.PAN_AADHAR_FRAGMENT);

        switch (requestCode) {
            case (MY_CAMERA_REQUEST_CODE): {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanNow();
                } else {
                    CustomLogger.getInstance().logDebug("onRequestPermissionsResult: Permission Denied", CustomLogger.Mask.PAN_AADHAR_FRAGMENT);
                }
                break;
            }
            default: {
                if (imagePicker != null)
                    imagePicker.onRequestPermissionsResult(this.getActivity(), requestCode, permissions, grantResults);
            }

        }

    }

    @Override
    public void updateState(State state) {
        this.state = state;
    }
}