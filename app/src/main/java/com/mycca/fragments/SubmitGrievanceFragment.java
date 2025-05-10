package com.mycca.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.google.gson.reflect.TypeToken;
import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.activity.VerificationActivity;
import com.mycca.adapter.GenericSpinnerAdapter;
import com.mycca.adapter.RecyclerViewAdapterSelectedImages;
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
import com.mycca.models.GrievanceModel;
import com.mycca.models.GrievanceType;
import com.mycca.models.SelectedImageModel;
import com.mycca.providers.CircleDataProvider;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.DataSubmissionAndMail;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.IOHelper;
import com.mycca.tools.Preferences;
import com.mycca.tools.VolleyHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;


public class SubmitGrievanceFragment extends MySubmittableFragment implements VolleyHelper.VolleyResponse, OnFABMenuSelectedListener {

    View view;
    TextInputEditText editTextPensionerIdentifier, editTextEmail, editTextMobile, editTextDetails;
    TextInputLayout textInputIdentifier;
    RadioGroup radioGroup;
    Spinner spinnerGrievance, spinnerSubmittedBy, spinnerCircle;
    Button submit, save;
    FloatingActionButton buttonChooseFile;
    TextView textViewSelectedFileCount;
    LinearLayout radioLayout;
    ProgressDialog progressDialog;
    ImagePicker imagePicker;

    //    boolean isUploadedToFirebase = false, isUploadedToServer = false, isOTPVerified = false;
    int counterUpload = 0, counterServerImages = 0, counterFirebaseImages;
    int hintResId, typeResId;
    public static final int REQUEST_OTP = 8543;
    String hint, code, mobile, email, details, type, submittedBy, refNo, prefix, savedModel;

    MainActivity mainActivity;
    GrievanceType grievanceType;
    GrievanceModel grievanceModel;
    Circle circle;
    VolleyHelper volleyHelper;
    GrievanceType[] list;
    ArrayList<Uri> firebaseImageURLs;
    private ArrayList<FABMenuItem> items;
    ArrayList<SelectedImageModel> selectedImageModelArrayList;
    RecyclerView recyclerViewSelectedImages;
    RecyclerViewAdapterSelectedImages adapterSelectedImages;
    GenericSpinnerAdapter<Circle> statesAdapter;
    GenericSpinnerAdapter<GrievanceType> adapter;
    ArrayAdapter<String> arrayAdapter1;

    public SubmitGrievanceFragment() {

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_grievance, container, false);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            typeResId = bundle.getInt("Type");
            type = getString(typeResId);
            savedModel = bundle.getString("SavedModel", null);
        }
        bindViews();
        setHasOptionsMenu(true);
        init();
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_form_data:
                clearFormData();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_form, menu);
    }

    private void bindViews() {

        volleyHelper = new VolleyHelper(this, getContext());
        recyclerViewSelectedImages = view.findViewById(R.id.recycler_view_selected_images);
        radioLayout = view.findViewById(R.id.layout_radio);
        textInputIdentifier = view.findViewById(R.id.text_input_pensioner_code);
        editTextPensionerIdentifier = view.findViewById(R.id.et_grievance_pcode);
        editTextMobile = view.findViewById(R.id.et_grievance_mobile);
        editTextEmail = view.findViewById(R.id.et_grievance_email);
        spinnerCircle = view.findViewById(R.id.spinner_grievance_circle);
        spinnerGrievance = view.findViewById(R.id.spinner_type);
        editTextDetails = view.findViewById(R.id.et_grievance_details);
        spinnerSubmittedBy = view.findViewById(R.id.spinner_submitted_by);
        textViewSelectedFileCount = view.findViewById(R.id.textview_selected_file_count_grievance);
        radioGroup = view.findViewById(R.id.radio_group_identifier_type);
        buttonChooseFile = view.findViewById(R.id.button_attach);
        submit = view.findViewById(R.id.button_submit);
        save = view.findViewById(R.id.button_save);
    }

    private void init() {

        mainActivity = (MainActivity) getActivity();
        progressDialog = new ProgressDialog(mainActivity != null ? mainActivity : getActivity());
        hintResId = R.string.p_code;
        hint = getString(hintResId);
        selectedImageModelArrayList = new ArrayList<>();
        initItems();

        editTextPensionerIdentifier.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_black_24dp, 0, 0, 0);
        editTextEmail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_black_24dp, 0, 0, 0);
        editTextMobile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone_android_black_24dp, 0, 0, 0);
        editTextDetails.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_detail, 0, 0, 0);

        statesAdapter = new GenericSpinnerAdapter<>(getContext(), CircleDataProvider.getInstance().getActiveCircleData());
        spinnerCircle.setAdapter(statesAdapter);

        if (typeResId == R.string.pension) {
            list = Helper.getInstance().getPensionGrievanceTypeList();
            radioLayout.setVisibility(View.GONE);
            prefix = "PEN";
        } else {
            radioLayout.setVisibility(View.VISIBLE);
            list = Helper.getInstance().getGPFGrievanceTypeList();
            prefix = "GPF";
        }
        adapter = new GenericSpinnerAdapter<>(getContext(), list);
        spinnerGrievance.setAdapter(adapter);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonPensioner:
                    hintResId = R.string.p_code;
                    editTextPensionerIdentifier.setFilters(Helper.getInstance().limitInputLength(15));
                    break;
                case R.id.radioButtonHR:
                    hintResId = R.string.hr_num;
                    editTextPensionerIdentifier.setFilters(new InputFilter[]{});
                    break;
                case R.id.radioButtonStaff:
                    hintResId = R.string.staff_num;
                    editTextPensionerIdentifier.setFilters(new InputFilter[]{});
            }
            editTextPensionerIdentifier.setText("");
            editTextPensionerIdentifier.setError(null);
            hint = getString(hintResId);
            textInputIdentifier.setHint(hint);
        });

        arrayAdapter1 = new ArrayAdapter<>(mainActivity, R.layout.simple_spinner, submittedByList());
        spinnerSubmittedBy.setAdapter(arrayAdapter1);

        final FABRevealMenu fabMenu = view.findViewById(R.id.fabMenu_submit_grievance);
        try {
            if (buttonChooseFile != null && fabMenu != null) {
                mainActivity.setFabRevealMenu(fabMenu);
                fabMenu.setMenuItems(items);   //set menu items from arraylist
                fabMenu.bindAnchorView(buttonChooseFile);//attach menu to fab
                fabMenu.setOnFABMenuSelectedListener(this); //set menu item selection
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        submit.setOnClickListener(v -> {
//            if (isUploadedToFirebase || isUploadedToServer)
            if (state != State.INIT)
                doSubmission();
            else if (checkInputBeforeSubmission())
                showConfirmSubmissionDialog();
        });

        save.setOnClickListener(v -> {
            if (checkInputBeforeSubmission())
                saveGrievanceOffline();
        });
        setSelectedFileCount(0);

        if (savedModel != null) {
            loadOfflineModelValues();
        }
        adapterSelectedImages = new RecyclerViewAdapterSelectedImages(selectedImageModelArrayList, this);
        recyclerViewSelectedImages.setAdapter(adapterSelectedImages);
        recyclerViewSelectedImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));


        firebaseImageURLs = new ArrayList<>();

    }

    private void loadOfflineModelValues() {
        GrievanceModel model = (GrievanceModel) Helper.getInstance().getObjectFromJson(savedModel, GrievanceModel.class);
        if (model.getGrievanceType() >= 100) {
            switch (model.getIdentifierType()) {
                case R.string.hr_num:
                    radioGroup.check(R.id.radioButtonHR);
                    hint = getString(R.string.hr_num);
                    break;
                case R.string.staff_num:
                    radioGroup.check(R.id.radioButtonStaff);
                    hint = getString(R.string.staff_num);
                    break;
                default:
                    radioGroup.check(R.id.radioButtonPensioner);
            }
        }
        textInputIdentifier.setHint(hint);
        editTextPensionerIdentifier.setText(model.getIdentifierNumber());
        editTextMobile.setText(model.getMobile());
        editTextEmail.setText(model.getEmail());
        editTextDetails.setText(model.getDetails());
        spinnerSubmittedBy.setSelection(arrayAdapter1.getPosition(model.getSubmittedBy()));
        spinnerGrievance.setSelection(adapter.getPosition(
                Helper.getInstance().getGrievanceFromId(model.getGrievanceType())));
        spinnerCircle.setSelection(statesAdapter.getPosition(
                CircleDataProvider.getInstance().getCircleFromCode(model.getCircle())));
        selectedImageModelArrayList = (ArrayList<SelectedImageModel>) Helper.getInstance().getImagesFromString(model.getFilePathList());

        if (selectedImageModelArrayList != null) {
            setSelectedFileCount(selectedImageModelArrayList.size());
            CustomLogger.getInstance().logDebug("Files found = " + selectedImageModelArrayList.size());
        } else {
            selectedImageModelArrayList = new ArrayList<>();
            CustomLogger.getInstance().logDebug("No File found");
        }
    }

    private void saveGrievanceOffline() {
        String fileList;
        if (selectedImageModelArrayList != null)
            fileList = Helper.getInstance().getStringFromList(selectedImageModelArrayList);
        else
            fileList = null;
        CustomLogger.getInstance().logDebug(fileList);
        GrievanceModel model = new GrievanceModel(hintResId, code, email, mobile, details,
                circle.getCode(),
                grievanceType.getId(),
                submittedBy, fileList);

        Type collectionType = new TypeToken<ArrayList<GrievanceModel>>() {
        }.getType();
        Helper.getInstance().saveModelOffline(mainActivity, model, collectionType, IOHelper.GRIEVANCES);
    }

    private boolean checkInputBeforeSubmission() {
        code = editTextPensionerIdentifier.getText().toString();
        mobile = editTextMobile.getText().toString();
        email = editTextEmail.getText().toString();
        details = editTextDetails.getText().toString();
        submittedBy = (String) spinnerSubmittedBy.getSelectedItem();
        circle = (Circle) spinnerCircle.getSelectedItem();
        grievanceType = (GrievanceType) spinnerGrievance.getSelectedItem();

        if (code.length() != 15 && hintResId == R.string.p_code) {
            editTextPensionerIdentifier.setError(getString(R.string.invalid_p_code));
            editTextPensionerIdentifier.requestFocus();
            return false;
        } else if (code.trim().isEmpty() && hintResId == R.string.hr_num) {
            editTextPensionerIdentifier.setError(getString(R.string.invalid_hr_num));
            editTextPensionerIdentifier.requestFocus();
            return false;
        } else if (code.trim().isEmpty() && hintResId == R.string.staff_num) {
            editTextPensionerIdentifier.setError(getString(R.string.invalid_staff_num));
            editTextPensionerIdentifier.requestFocus();
            return false;
        } else if (mobile.length() < 10) {
            editTextMobile.setError(getString(R.string.invalid_mobile));
            editTextMobile.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError(getString(R.string.invalid_email));
            editTextEmail.requestFocus();
            return false;
        }
// else if (details.trim().isEmpty()) {
//            editTextDetails.setError(getString(R.string.empty_detail));
//            editTextDetails.requestFocus();
//            return false;
//        }
        return true;
    }

    private void initItems() {
        items = new ArrayList<>();
        items.add(new FABMenuItem(0, getString(R.string.add_image), AppCompatResources.getDrawable(mainActivity, R.drawable.ic_attach_file_white_24dp)));
        items.add(new FABMenuItem(1, getString(R.string.remove_all), AppCompatResources.getDrawable(mainActivity, R.drawable.ic_close_24dp)));
    }

    private String[] submittedByList() {
        String first;
        if (typeResId == R.string.pension)
            first = getString(R.string.pensioner);
        else
            first = getString(R.string.gpf_benificiary);
        return new String[]{first, getString(R.string.other)};
    }

    public void setSelectedFileCount(int count) {
        textViewSelectedFileCount.setText(String.format(getString(R.string.files_selected), String.valueOf(count)));
    }

    private void removeAllSelectedImages() {
        if (selectedImageModelArrayList == null || adapterSelectedImages == null) {
            return;
        }
        selectedImageModelArrayList.clear();
        adapterSelectedImages.notifyDataSetChanged();
        setSelectedFileCount(0);
    }

    private void showConfirmSubmissionDialog() {
        Helper.getInstance().hideKeyboardFrom(mainActivity);
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_confirm_submission, (ViewGroup) this.getView(), false);
        loadValues(v);
        Helper.getInstance().getConfirmationDialog(mainActivity, v,
                (dialog, which) -> doSubmission());
    }

    private void loadValues(View v) {
        TextView pensionerHeading = v.findViewById(R.id.textview_confirm1);
        TextView pensionerValue = v.findViewById(R.id.textview_confirm1_value);
        TextView mobNo = v.findViewById(R.id.textview_confirm2_value);
        TextView emailValue = v.findViewById(R.id.textview_confirm3_value);
        TextView circle = v.findViewById(R.id.textview_confirm4_value);
        TextView grievance = v.findViewById(R.id.textview_confirm5_value);
        TextView gr_by = v.findViewById(R.id.textview_confirm6_value);
        TextView grievanceDetails = v.findViewById(R.id.textview_confirm7_value);

        pensionerHeading.setText(hint);
        pensionerValue.setText(code);
        mobNo.setText(mobile);
        emailValue.setText(email);
        grievanceDetails.setText(details.trim());
        grievance.setText(grievanceType.getName());
        gr_by.setText(spinnerSubmittedBy.getSelectedItem().toString());
        circle.setText(Preferences.getInstance().getStringPref(mainActivity, Preferences.PREF_LANGUAGE)
                .equals("hi") ? this.circle.getHi() : this.circle.getEn());
    }

    private void doSubmission() {
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                CustomLogger.getInstance().logDebug("version checked =" + Helper.versionChecked);
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
                Helper.getInstance().noInternetDialog(mainActivity);
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    public void otp() {

        Intent intent = new Intent(mainActivity, VerificationActivity.class)
                .putExtra(VerificationActivity.INTENT_PHONENUMBER, mobile)
                .putExtra(VerificationActivity.INTENT_COUNTRY_CODE, "91");
        mainActivity.startActivityForResult(intent, REQUEST_OTP);
    }

    private void doSubmissionOnInternetAvailable() {

//        CustomLogger.getInstance().logDebug("doSubmissionOnInternetAvailable: \n Firebase = " + isUploadedToFirebase + "\n" +
//                "Server = " + isUploadedToServer);
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
    }

    private void uploadDataToFirebase() {
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        Transaction.Handler handler = new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                CustomLogger.getInstance().logDebug("doTransaction: " + mutableData.getValue());
                long count = 0;
                if (mutableData.getValue() != null) {
                    count = (long) mutableData.getValue();
                }

                // Set value and report transaction success
                mutableData.setValue(++count);
                CustomLogger.getInstance().logDebug("count= " + count);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (b) {
                    refNo = prefix + "-" + circle.getCode() + "-" + dataSnapshot.getValue();
                    uploadData();
                } else {
                    progressDialog.dismiss();
                    CustomLogger.getInstance().logDebug("database error: " +
                            "Details" + databaseError.getDetails() +
                            "Message = " + databaseError.getMessage() +
                            "Code = " + databaseError.getCode() +
                            "Snapshot = " + dataSnapshot.toString());
                    Helper.getInstance().showMaintenanceDialog(mainActivity, circle.getCode());
                }
            }

        };
        FireBaseHelper.getInstance().getReferenceNumber(handler, circle.getCode());
    }

    private void uploadData() {
        grievanceModel = new GrievanceModel(hintResId, code, email, mobile, details, submittedBy, circle.getCode(),
                FireBaseHelper.getInstance().getAuth().getUid(),
                refNo, new Date(), 0,
                grievanceType.getId(),
                selectedImageModelArrayList.size());

        Task<Void> task = FireBaseHelper.getInstance().uploadDataToFireBase(circle.getCode(),
                grievanceModel,
                FireBaseHelper.ROOT_GRIEVANCES,
                code, String.valueOf(grievanceType.getId()));

        task.addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                uploadAllImagesToFirebase();
            } else {
                progressDialog.dismiss();
                Helper.getInstance().showMaintenanceDialog(mainActivity, circle.getCode());
                CustomLogger.getInstance().logDebug("uploadData: Failed Message= " + Objects.requireNonNull(task1.getException()).getMessage());
                CustomLogger.getInstance().logDebug("uploadData: Failed Cause= " + task1.getException().getCause());
                CustomLogger.getInstance().logDebug("uploadData: Failed Result= " + task1.getResult());
            }
        });

    }

    private void uploadAllImagesToFirebase() {
        if (selectedImageModelArrayList.size() > 0) {

            progressDialog.setMessage(getString(R.string.uploading_files));
            counterFirebaseImages = 0;
            counterUpload = 0;

            for (SelectedImageModel imageModel : selectedImageModelArrayList) {
                final UploadTask uploadTask = FireBaseHelper.getInstance().uploadFiles(circle.getCode(),
                        imageModel,
                        true,
                        counterFirebaseImages++,
                        FireBaseHelper.ROOT_GRIEVANCES,
                        code,
                        String.valueOf(grievanceType.getId()),
                        FireBaseHelper.ROOT_BY_USER);

                if (uploadTask != null) {
                    uploadTask.addOnFailureListener(
                            exception -> {
                                CustomLogger.getInstance().logDebug("onFailure: " + exception.getMessage());
                                showError(getString(R.string.file_upload_error), getString(R.string.file_not_uploaded));
                            })
                            .addOnSuccessListener(
                                    taskSnapshot -> {
                                        // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                            firebaseImageURLs.add(uri);
                                            progressDialog.setMessage(String.format(getString(R.string.uploaded_file), String.valueOf(++counterUpload), String.valueOf(selectedImageModelArrayList.size())));
                                            CustomLogger.getInstance().logDebug("onSuccess: counter = " + counterUpload + "size = " + selectedImageModelArrayList.size());
                                            if (counterUpload == selectedImageModelArrayList.size()) {
                                                updateState(State.UPLOADED_TO_FIREBASE);
//                                                isUploadedToFirebase = true;
                                                doSubmission();
                                            }
                                        });
                                    });
                }
            }
        } else {
            updateState(State.UPLOADED_TO_FIREBASE);
//            isUploadedToFirebase = true;
            doSubmission();
        }
    }

    public void showError(String title, String message) {
        progressDialog.dismiss();
        Helper.getInstance().showErrorDialog(message, title, mainActivity);
    }

    private void uploadImagesToServer() {

        counterServerImages = 0;
        progressDialog.setMessage(getString(R.string.processing));
        int totalFilesToAttach = selectedImageModelArrayList.size();

        if (totalFilesToAttach != 0) {
            DataSubmissionAndMail.getInstance().uploadImagesToServer(firebaseImageURLs,
                    code,
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
        String url = Helper.getInstance().getAPIUrl() + "sendGrievanceEmail.php/";
        Map<String, String> params = new HashMap<>();

        String hint_en = Helper.getInstance().getEnglishString(hintResId);
        String type_en = Helper.getInstance().getEnglishString(typeResId);
        String subType_en = Helper.getInstance().getGrievanceString(grievanceType.getId(), Locale.ENGLISH);
        String submittedBy_en = Helper.getInstance().getEnglishString(getSubmittedByResId());
        CustomLogger.getInstance().logDebug(hint_en + ", " + type_en);

        //params.put("mailTo",circle.getMails());
        params.put("pensionerCode", code);
        params.put("personType", hint_en);
        params.put("refNo", refNo);
        params.put("folder", DataSubmissionAndMail.SUBMIT);
        params.put("pensionerMobileNumber", mobile);
        params.put("pensionerEmail", email);
        params.put("grievanceType", type_en);
        params.put("grievanceSubType", subType_en);
        params.put("grievanceDetails", details);
        params.put("grievanceSubmittedBy", submittedBy_en);
        params.put("fileCount", selectedImageModelArrayList.size() + "");

        DataSubmissionAndMail.getInstance().sendMail(params, "send_mail-" + code, volleyHelper, url);
    }

    private int getSubmittedByResId() {

        if (submittedBy.equals(getString(R.string.pensioner)))
            return R.string.pensioner;
        else if (submittedBy.equals(getString(R.string.gpf_benificiary)))
            return R.string.gpf_benificiary;
        else
            return R.string.other;
    }

    private void setSubmissionSuccessForGrievance() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("submissionSuccess", true);
        FireBaseHelper.getInstance().updateData(circle.getCode(),
                String.valueOf(grievanceType.getId()),
                hashMap,
                FireBaseHelper.ROOT_GRIEVANCES,
                code
        );

        FireBaseHelper.getInstance().uploadDataToFireBase(circle.getCode(), code, FireBaseHelper.ROOT_REF_NUMBERS, refNo);
    }

    private void clearFormData() {
        editTextPensionerIdentifier.setText("");
        editTextMobile.setText("");
        editTextEmail.setText("");
        spinnerGrievance.setSelection(0);
        spinnerSubmittedBy.setSelection(0);
        spinnerCircle.setSelection(0);
        editTextDetails.setText("");
        removeAllSelectedImages();
    }

    private void showImageChooser() {
        imagePicker = Helper.getInstance().showImageChooser(imagePicker, mainActivity, true, new ImagePicker.Callback() {
            @Override
            public void onPickImage(Uri imageUri) {
                CustomLogger.getInstance().logDebug("onPickImage: " + imageUri.getPath());

            }

            @Override
            public void onCropImage(Uri imageUri) {
                CustomLogger.getInstance().logDebug("onCropImage: " + imageUri.getPath());
                int currentPosition = selectedImageModelArrayList.size();
                selectedImageModelArrayList.add(currentPosition, new SelectedImageModel(imageUri));
                adapterSelectedImages.notifyItemInserted(currentPosition);
                adapterSelectedImages.notifyDataSetChanged();
                CustomLogger.getInstance().logDebug("onCropImage: Item inserted at " + currentPosition);
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
                CustomLogger.getInstance().logDebug("onPermissionDenied: Permission not given to choose textViewMessage");
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
                removeAllSelectedImages();
                break;
        }
    }

    @Override
    public void onResponse(String str) {
        JSONObject jsonObject = Helper.getInstance().getJson(str);
        CustomLogger.getInstance().logDebug(jsonObject.toString());
        try {
            if (jsonObject.get("action").equals("Creating Image")) {
                counterServerImages++;
                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {
                    if (counterServerImages == selectedImageModelArrayList.size()) {
                        CustomLogger.getInstance().logDebug("onResponse: Files uploaded");
                        updateState(State.UPLOADED_TO_SERVER);
//                        isUploadedToServer = true;
                        doSubmission();
                    }
                } else {
                    showError(getString(R.string.file_upload_error), getString(R.string.file_not_uploaded));
                    CustomLogger.getInstance().logDebug("onResponse: Image = " + counterServerImages + " failed");
                }
            } else if (jsonObject.getString("action").equals("Sending Mail")) {
                if (jsonObject.get("result").equals(volleyHelper.SUCCESS)) {

                    progressDialog.dismiss();
                    Helper.getInstance().showMessage(mainActivity,
                            String.format(getString(R.string.grievance_submission_success), type, grievanceType.getName(), refNo),
                            getString(R.string.success),
                            FancyAlertDialogType.SUCCESS);
                    updateState(State.INIT);
//                    isOTPVerified = isUploadedToServer = isUploadedToFirebase = false;
                    setSubmissionSuccessForGrievance();
                } else {
                    showError(getString(R.string.failure), getString(R.string.grievance_submission_fail));
                }
            }
        } catch (JSONException jse) {
            jse.printStackTrace();
            showError(getString(R.string.some_error), getString(R.string.try_again));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLogger.getInstance().logDebug("onActivityResult: " + requestCode + " ," + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (imagePicker != null)
            imagePicker.onActivityResult(mainActivity, requestCode, resultCode, data);
        if (requestCode == REQUEST_OTP) {
            if (resultCode == RESULT_OK) {
                CustomLogger.getInstance().logDebug("Verification complete");
                updateState(State.OTP_VERIFIED);
//                isOTPVerified = true;
                doSubmissionOnInternetAvailable();
            } else {
                Helper.getInstance().showErrorDialog(getString(R.string.try_again), getString(R.string.failed), mainActivity);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        CustomLogger.getInstance().logDebug("onRequestPermissionsResult: " + "Inspection");

        switch (requestCode) {
            default: {
                if (imagePicker != null)
                    imagePicker.onRequestPermissionsResult(mainActivity, requestCode, permissions, grantResults);
            }

        }

    }

    @Override
    public void onError(VolleyError volleyError) {
        volleyError.printStackTrace();
        showError(getString(R.string.some_error), getString(R.string.try_again));
    }


    @Override
    public void updateState(State state) {
        this.state = state;

    }
}

//    private void showTutorial() {
//
//        final FancyShowCaseView fancyShowCaseView1 = new FancyShowCaseView.Builder(mainActivity)
//                .focusOn(view.findViewById(R.id.button_attach))
//                .focusShape(FocusShape.CIRCLE)
//                .title("Add images using this button")
//                .fitSystemWindows(true)
//                .build();
//
//        final FancyShowCaseView fancyShowCaseView2 = new FancyShowCaseView.Builder(mainActivity)
//                .focusOn(menuClearForm)
//                .focusShape(FocusShape.CIRCLE)
//                .focusCircleRadiusFactor(2)
//                .title("Click to clear form data")
//                .build();
//
//        mainActivity.setmQueue(new FancyShowCaseQueue()
//                .add(fancyShowCaseView1)
//                .add(fancyShowCaseView2));
//        mainActivity.getmQueue().setCompleteListener(() -> mainActivity.setmQueue(null));
//        mainActivity.getmQueue().show();
//    }