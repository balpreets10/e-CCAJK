package com.mycca.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.gms.tasks.Task;
import com.mycca.R;
import com.mycca.adapter.GenericSpinnerAdapter;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.models.Circle;
import com.mycca.models.PensionFileStatusModel;
import com.mycca.providers.CircleDataProvider;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;


public class UpdatePensionFileFragment extends Fragment {

    Spinner spinnerCircles, spinnerFileStatus;
    TextInputEditText etHRNumber, etMobile, etMail;
    Button submit;

    String hRNum, mobile, email, fileStatus;

    public UpdatePensionFileFragment() {
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_pension_file, container, false);
        bindViews(view);
        init();
        return view;
    }

    private void bindViews(View view) {
        spinnerCircles = view.findViewById(R.id.spinner_update_pension_circles);
        spinnerFileStatus = view.findViewById(R.id.spinner_update_pension_status);
        etHRNumber = view.findViewById(R.id.et_update_pension_hr);
        etMobile = view.findViewById(R.id.et_update_pension_mobile);
        etMail = view.findViewById(R.id.et_update_pension_email);
        submit = view.findViewById(R.id.button_update_pension);
    }

    private void init() {
        etHRNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_black_24dp, 0, 0, 0);
        etMobile.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_phone_android_black_24dp, 0, 0, 0);
        etMail.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_email_black_24dp, 0, 0, 0);

        GenericSpinnerAdapter statesAdapter = new GenericSpinnerAdapter<>(getContext(), CircleDataProvider.getInstance().getActiveCircleData());
        spinnerCircles.setAdapter(statesAdapter);

        submit.setOnClickListener(v -> {
            hRNum = etHRNumber.getText().toString().trim();
            if (!hRNum.isEmpty())
                checkConnection();
            else
                etHRNumber.setError(getString(R.string.invalid_hr_num));
        });
    }

    private void checkConnection() {
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                updatePensionFileStatus();
            }

            @Override
            public void OnConnectionNotAvailable() {
                Helper.getInstance().noInternetDialog(getActivity());
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    private void updatePensionFileStatus() {
        fileStatus = (String) spinnerFileStatus.getSelectedItem();
        mobile = etMobile.getText().toString().trim();
        email = etMail.getText().toString().trim();

        String state = ((Circle) spinnerCircles.getSelectedItem()).getCode();
        PensionFileStatusModel model = new PensionFileStatusModel(hRNum, fileStatus, email, mobile);

        Task<Void> task = FireBaseHelper.getInstance().uploadDataToFireBase(state, model, FireBaseHelper.ROOT_PENSION_STATUS, hRNum);
        task.addOnSuccessListener(o -> {
            sendMessageToUser();
            sendMailToUser();
        });
    }

    private void sendMailToUser() {
        if (!email.isEmpty()) {
        }
    }

    private void sendMessageToUser() {
        if (!mobile.isEmpty()) {
        }
    }
}
