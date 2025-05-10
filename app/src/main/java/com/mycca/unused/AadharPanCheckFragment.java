package com.mycca.unused;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mycca.activity.MainActivity;
import com.mycca.R;
import com.google.firebase.database.DatabaseReference;

public class AadharPanCheckFragment extends Fragment {

    String TAG = "Check";
    int type;
    String typeName;
    DatabaseReference numberRef;

    ProgressDialog progressDialog;
    Button upload, check;
    TextInputEditText pcode;
    TextView optionalMessage, statusMessage;


    public AadharPanCheckFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_aadhar_pan_check, container, false);
        Bundle bundle = this.getArguments();
        type = bundle.getInt("UploadType");
        init(view, type);
        return view;
    }

    private void init(View view, final int type) {

        pcode = view.findViewById(R.id.edittext_pcode);
        progressDialog = new ProgressDialog(view.getContext());
        optionalMessage = view.findViewById(R.id.textView_message);
        statusMessage = view.findViewById(R.id.textView_status);

        upload = view.findViewById(R.id.btn_upload);
        upload.setVisibility(View.GONE);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.putExtra("UploadType", type);
                intent.putExtra("PensionerCode", pcode.getText().toString());
                startActivity(intent);
            }
        });

        /*details = view.findViewById(R.id.btn_view);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PanAdhaarHistoryActivity.class);
                intent.putExtra("UploadType", type);
                intent.putExtra("PensionerCode", pcode.getText().toString());
                startActivity(intent);
            }
        });*/

        check = view.findViewById(R.id.btn_check_status);
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pcode.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a textViewPensionerCode code", Toast.LENGTH_SHORT).show();
                } else {
                    check.setVisibility(View.GONE);
                    checkStatus();
                }
            }
        });

    }

    private void checkStatus() {
        /*progressDialog.show();
        if (type == Helper.getInstance().UPLOAD_TYPE_ADHAAR) {
            numberRef = FireBaseHelper.getInstance().versionedDbRef.child(FireBaseHelper.getInstance().ROOT_ADHAAR).child(pcode.getText().toString());
            typeName = "Aadhaar";
            progressDialog.setMessage("Checking Aadhar Verification Status\nPlease Wait...");
        } else {
            numberRef = FireBaseHelper.getInstance().versionedDbRef.child(FireBaseHelper.getInstance().ROOT_PAN).child(pcode.getText().toString());
            typeName = "PAN";
            progressDialog.setMessage("Checking PAN Verification Status\nPlease Wait...");
        }
        numberRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, dataSnapshot.toString());
                if (dataSnapshot.getValue() != null) {

                    long textViewStatus = (long) dataSnapshot.child("textViewStatus").getValue();
                    String number = (String) dataSnapshot.child("number").getValue();
                    String msg = (String) dataSnapshot.child("msg").getValue();
                    Log.d(TAG, String.valueOf(textViewStatus));
                    setMessageAndAction(textViewStatus,msg,number);
                    *//*numberRef.child("textViewStatus").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long textViewStatus = (long) dataSnapshot.getValue();
                            setMessageAndAction(textViewStatus);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d( "onCancelled: ",databaseError.getMessage());

                        }
                    });*//*
                } else {
                    setMessageAndAction(-1,null,null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Cancelled");
                setMessageAndAction(100,null,null);
            }
        });
    */
    }

    private void setMessageAndAction(long status, String msg, String number) {
        switch ((int) status) {
            case -1:
                statusMessage.setText(typeName + " Number not updated");
                optionalMessage.setText(msg);
                upload.setVisibility(View.VISIBLE);
                break;
            case 0:
            case 1:
                statusMessage.setText(typeName + " Number Updation under process");
                optionalMessage.setText(msg);
                break;
            case 2:
                statusMessage.setText(typeName + " Number Updation Failed");
                optionalMessage.setText(msg);
                upload.setVisibility(View.VISIBLE);
                break;
            case 3:
                statusMessage.setText("Your " + typeName + " Number is updated\n\n" + typeName + " Number = " + number);
                break;
            case 100:
                statusMessage.setText("Some Error Occured. Please Try Again");
                check.setVisibility(View.VISIBLE);
        }

        progressDialog.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        upload.setVisibility(View.GONE);
        check.setVisibility(View.VISIBLE);
        statusMessage.setText("");
        optionalMessage.setText("");
    }
}
