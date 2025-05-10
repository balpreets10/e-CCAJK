package com.mycca.unused;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.mycca.adapter.RecyclerViewAdapterTracking;
import com.mycca.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class PanAdhaarHistoryActivity extends AppCompatActivity {

    String pcode, status, TAG = "History";
    int type;
    DataSnapshot ds;
    TextView textView;
    RecyclerView recyclerView;
    RecyclerViewAdapterTracking historyAdapter;
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    ArrayList<PanAdhaarStatus> panAdhaarStatusArrayList = new ArrayList<>();
    DatabaseReference dbref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pan_adhaar_history);
        pcode = getIntent().getStringExtra("PensionerCode");
        type = getIntent().getIntExtra("UploadType", -1);
        //getSupportActionBar().setTitle((type == Helper.getInstance().UPLOAD_TYPE_ADHAAR ? "Aadhaar Updation Status" : "Pan Updation Status"));
        init();
    }

    private void init() {

        textView = findViewById(R.id.textview_current);
        recyclerView = findViewById(R.id.recyclerview_history);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        //historyAdapter = new RecyclerViewAdapterTracking(panAdhaarStatusArrayList);
        recyclerView.setAdapter(historyAdapter);
        getHistory();
        Log.d(TAG, "init: " + panAdhaarStatusArrayList);

    }

    private void getHistory() {
        /*if (type == Helper.getInstance().UPLOAD_TYPE_ADHAAR)
            dbref = FireBaseHelper.getInstance().versionedDbRef.child(FireBaseHelper.getInstance().ROOT_ADHAAR_STATUS).child(pcode);
        else
            dbref = FireBaseHelper.getInstance().versionedDbRef.child(FireBaseHelper.getInstance().ROOT_PAN_STATUS).child(pcode);

        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                panAdhaarStatusArrayList.clear();
                if (dataSnapshot.getChildrenCount() > 0) {
                    PanAdhaarStatus panAdhaarStatus = null;
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                        panAdhaarStatus = dataSnapshot1.getValue(PanAdhaarStatus.class);
                        Log.d(TAG, panAdhaarStatus.getAppliedDate().toString());
                        textViewStatus = FireBaseHelper.getInstance().getStatusString(panAdhaarStatus.getStatus());
                        panAdhaarStatusArrayList.add(panAdhaarStatus);
                        historyAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.d(TAG, "No records found");
                    textViewStatus = "Not Applied for Updation";
                }
                textView.setText("Current Status: " + textViewStatus);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }
}


