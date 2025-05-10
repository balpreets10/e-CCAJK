package com.mycca.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.ValueEventListener;
import com.mycca.R;
import com.mycca.adapter.RecyclerViewAdapterTracking;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.listeners.ClickListener;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.listeners.RecyclerViewTouchListeners;
import com.mycca.models.GrievanceModel;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.LocaleHelper;

import java.util.ArrayList;

public class TrackGrievanceResultActivity extends AppCompatActivity {

    RecyclerView recyclerViewTrack;
    TextView textView;
    ArrayList<GrievanceModel> grievanceModelArrayList;
    RecyclerViewAdapterTracking adapterTracking;
    String pensionerCode, state;
    ProgressDialog progressDialog;
    final String TAG = "Track";
    long grievanceType = -1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_grievance_result);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getString(R.string.track_grievances));
        init();

    }

    private void init() {

        progressDialog = Helper.getInstance().getProgressWindow(this, getString(R.string.checking_grievance));
        progressDialog.show();

        grievanceModelArrayList = new ArrayList<>();
        adapterTracking = new RecyclerViewAdapterTracking(grievanceModelArrayList, this);

        textView = findViewById(R.id.textview_tracking);
        textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_exclamation, 0, 0);

        recyclerViewTrack = findViewById(R.id.recyclerview_tracking);
        recyclerViewTrack.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTrack.setAdapter(adapterTracking);
        recyclerViewTrack.addOnItemTouchListener(new RecyclerViewTouchListeners(this, recyclerViewTrack, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                CustomLogger.getInstance().logDebug("onClick: " + position, CustomLogger.Mask.TRACK_GRIEVANCE_ACTIVITY);
                grievanceModelArrayList.get(position).setExpanded(!grievanceModelArrayList.get(position).isExpanded());
                if (grievanceModelArrayList.get(position).isHighlighted())
                    grievanceModelArrayList.get(position).setHighlighted(false);
                adapterTracking.notifyItemChanged(position);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        ManageNoGrievanceLayout(true);
        textView.setText(getResources().getString(R.string.no_grievance));

        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                // ATTENTION: This was auto-generated to handle app links.
                Intent appLinkIntent = getIntent();
                String appLinkAction = appLinkIntent.getAction();
                Uri appLinkData = appLinkIntent.getData();
                if(appLinkData != null)
                {
                    String[] details = appLinkData.getLastPathSegment().split("-");
                    pensionerCode = details[0];
                    state = details[1];
                    grievanceType = Long.parseLong(details[2]);
                    getGrievances();
                }else {
                    getGrievancesOnConnectionAvailable();
                }
            }

            @Override
            public void OnConnectionNotAvailable() {
                onConnectionNotAvailable();
                ManageNoGrievanceLayout(true);
                textView.setText(getResources().getString(R.string.no_internet));
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    private void ManageNoGrievanceLayout(boolean show) {
        if (show) {
            recyclerViewTrack.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewTrack.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }
    }

    private void onConnectionNotAvailable() {
        progressDialog.dismiss();
        Helper.getInstance().showFancyAlertDialog(this,
                getString(R.string.connect_to_internet),
                getString(R.string.track_grievances),
                getString(R.string.ok),
                null,
                null,
                null,
                FancyAlertDialogType.ERROR);
    }

    private void getGrievancesOnConnectionAvailable() {

        pensionerCode = getIntent().getStringExtra("Code");
        state = getIntent().getStringExtra("Circle");
        try {
            grievanceType = getIntent().getLongExtra("grievanceType", -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        getGrievances();
    }

    private void getGrievances() {
        CustomLogger.getInstance().logDebug("getGrievances for: " + pensionerCode, CustomLogger.Mask.TRACK_GRIEVANCE_ACTIVITY);

        FireBaseHelper.getInstance().getDataFromFireBase(state, new ChildEventListener() {
            int i = 0;
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, String s) {
                CustomLogger.getInstance().logDebug("grievance key:" + dataSnapshot.getKey(), CustomLogger.Mask.TRACK_GRIEVANCE_ACTIVITY);
                try {
                    GrievanceModel model = dataSnapshot.getValue(GrievanceModel.class);
                    if (model != null) {
                            if (grievanceType != -1) {
                            if (model.getGrievanceType() == grievanceType) {
                                model.setExpanded(true);
                                model.setHighlighted(true);
                            }
                        }
                        if (model.isSubmissionSuccess()) {
                            grievanceModelArrayList.add(i, model);
                            CustomLogger.getInstance().logDebug("arraylist size:" + grievanceModelArrayList.size(), CustomLogger.Mask.TRACK_GRIEVANCE_ACTIVITY);
                            adapterTracking.notifyItemInserted(i);
                            i++;
                        }
                    }
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                CustomLogger.getInstance().logDebug("ChildChanged\n" + dataSnapshot, CustomLogger.Mask.TRACK_GRIEVANCE_ACTIVITY);
                GrievanceModel model = dataSnapshot.getValue(GrievanceModel.class);
                int counter = 0;
                if (model != null) {
                    for (GrievanceModel gm : grievanceModelArrayList) {
                        if (gm.getGrievanceType() == model.getGrievanceType()) {
                            grievanceModelArrayList.remove(gm);
                            model.setExpanded(true);
                            model.setHighlighted(true);
                            grievanceModelArrayList.add(counter, model);
                            break;
                        }
                        counter++;
                    }
                    adapterTracking.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                CustomLogger.getInstance().logDebug("onCancelled: " + databaseError.getMessage(), CustomLogger.Mask.TRACK_GRIEVANCE_ACTIVITY);
                progressDialog.dismiss();
            }
        }, FireBaseHelper.ROOT_GRIEVANCES, pensionerCode);

        FireBaseHelper.getInstance().getDataFromFireBase(state, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                if (grievanceModelArrayList.size() > 0)
                    ManageNoGrievanceLayout(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        }, true, FireBaseHelper.ROOT_GRIEVANCES, pensionerCode);
    }

}

