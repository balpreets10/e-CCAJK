package com.mycca.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.mycca.R;
import com.mycca.activity.TrackGrievanceResultActivity;
import com.mycca.adapter.GenericSpinnerAdapter;
import com.mycca.models.Circle;
import com.mycca.providers.CircleDataProvider;
import com.mycca.tools.Helper;

public class TrackGrievanceFragment extends Fragment {

    private EditText editText;
    private TextInputLayout textInputLayout;
    private Spinner spinner;
    private RadioGroup radioGroup;
    private Button track;
    private Activity activity;
    private String hint = "Pensioner Code";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_grievance, container, false);
        bindView(view);
        init();
        return view;
    }

    private void bindView(View view) {
        editText = view.findViewById(R.id.edittext_pcode);
        textInputLayout = view.findViewById(R.id.text_input_layout);
        spinner = view.findViewById(R.id.spinner_track_circle);
        radioGroup = view.findViewById(R.id.radio_group_identifier_type);
        track = view.findViewById(R.id.btn_check_status);

    }

    private void init() {
        activity = getActivity();
        GenericSpinnerAdapter<Circle> circleAdapter = new GenericSpinnerAdapter<>(activity,
                CircleDataProvider.getInstance().getActiveCircleData());
        spinner.setAdapter(circleAdapter);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radioButtonPensioner:
                    hint = getString(R.string.p_code);
                    editText.setFilters(Helper.getInstance().limitInputLength(15));
                    break;
                case R.id.radioButtonHR:
                    hint = getString(R.string.hr_num);
                    editText.setFilters(new InputFilter[]{});
                    break;
                case R.id.radioButtonStaff:
                    hint = getString(R.string.staff_num);
                    editText.setFilters(new InputFilter[]{});
            }
            editText.setText("");
            textInputLayout.setHint(hint);
        });

        track.setOnClickListener(v -> {
            String code = editText.getText().toString().trim();
            if (code.length() != 15 && hint.equals(activity.getString(R.string.p_code))) {
                Toast.makeText(activity,
                        getString(R.string.invalid_p_code),
                        Toast.LENGTH_LONG).show();
            } else if (code.trim().isEmpty() && hint.equals(activity.getString(R.string.hr_num))) {
                Toast.makeText(activity,
                        getString(R.string.invalid_hr_num),
                        Toast.LENGTH_LONG).show();
            } else if (code.trim().isEmpty() && hint.equals(activity.getString(R.string.staff_num))) {
                Toast.makeText(activity,
                        getString(R.string.invalid_staff_num),
                        Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(activity, TrackGrievanceResultActivity.class);
                intent.putExtra("Code", editText.getText().toString());
                intent.putExtra("Circle", ((Circle) spinner.getSelectedItem()).getCode());
                startActivity(intent);
            }
            editText.requestFocus();
        });


    }
}
