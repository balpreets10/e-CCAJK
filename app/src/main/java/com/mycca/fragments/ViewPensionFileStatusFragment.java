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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mycca.R;
import com.mycca.adapter.GenericSpinnerAdapter;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.models.Circle;
import com.mycca.providers.CircleDataProvider;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;


public class ViewPensionFileStatusFragment extends Fragment {

    Spinner spinnerCircles;
    TextInputEditText etHRNumber;
    Button submit;

    String hRNum;


    public ViewPensionFileStatusFragment() {}


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_view_pension_file_status, container, false);
        bindViews(view);
        init();
        return view;
    }
    private void bindViews(View view) {
        spinnerCircles = view.findViewById(R.id.spinner_update_pension_circles);
        etHRNumber = view.findViewById(R.id.et_update_pension_hr);
        submit = view.findViewById(R.id.button_update_pension);
    }

    private void init() {
        etHRNumber.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_black_24dp, 0, 0, 0);
        
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
                getPensionFileStatus();
            }

            @Override
            public void OnConnectionNotAvailable() {
                Helper.getInstance().noInternetDialog(getActivity());
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    private void getPensionFileStatus() {
        
        String state = ((Circle) spinnerCircles.getSelectedItem()).getCode();
       
        FireBaseHelper.getInstance().getDataFromFireBase(state, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status= (String) dataSnapshot.child("pensionFileStatus").getValue();
                showStatus(status);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        },false,FireBaseHelper.ROOT_PENSION_STATUS,hRNum);
    }

    private void showStatus(String status) {
    }

}
