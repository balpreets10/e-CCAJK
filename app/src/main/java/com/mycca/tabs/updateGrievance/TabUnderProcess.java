package com.mycca.tabs.updateGrievance;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.adapter.RecyclerViewAdapterGrievanceUpdate;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.models.GrievanceModel;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.Helper;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Preferences;

import java.util.ArrayList;

public class TabUnderProcess extends Fragment {

    final String TAG = "UnderProcess";

    RecyclerView recyclerView;
    RelativeLayout relativeLayoutEmptyList;
    TextView textViewNoListInfo;
    ProgressDialog progressDialog;

    RecyclerViewAdapterGrievanceUpdate adapter;
    ArrayList<GrievanceModel> processingGrievances;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_grievances, container, false);
        bindViews(view);
        showEmptyListLayout(true);
        init();
        return view;
    }

    private void showEmptyListLayout(boolean show) {
        if (show) {
            recyclerView.setVisibility(View.GONE);
            relativeLayoutEmptyList.setVisibility(View.VISIBLE);
            if (getActivity() != null && isAdded())
                textViewNoListInfo.setText(R.string.no_grievances_processing);

        } else {
            relativeLayoutEmptyList.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_grievances);
        relativeLayoutEmptyList = view.findViewById(R.id.layout_empty_list);
        textViewNoListInfo = view.findViewById(R.id.textview_info_tab_grievances);
        progressDialog = Helper.getInstance().getProgressWindow(getActivity(), getString(R.string.please_wait));
    }

    private void init() {
        progressDialog.show();
        processingGrievances = new ArrayList<>();
        CustomLogger.getInstance().logDebug( "init: " + processingGrievances);
        adapter = new RecyclerViewAdapterGrievanceUpdate(processingGrievances, (MainActivity) getActivity(), false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        getData();
    }

    private void getData() {

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        GrievanceModel grievanceModel = ds.getValue(GrievanceModel.class);
                        if (grievanceModel != null && grievanceModel.isSubmissionSuccess()) {
                            if (grievanceModel.getGrievanceStatus() == 1) {
                                processingGrievances.add(grievanceModel);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                ArrayList<GrievanceModel> temp1=new ArrayList<>();
                CustomLogger.getInstance().logDebug( "\nonChildChanged: Arraylist" + processingGrievances);
                if (dataSnapshot.getValue() != null) {
                    String code=dataSnapshot.getKey();
                    for(GrievanceModel model:processingGrievances){
                        if(model.getIdentifierNumber().equals(code))
                            temp1.add(model);
                    }
                    processingGrievances.removeAll(temp1);
                    CustomLogger.getInstance().logDebug( "\nonChildChanged: Arraylist after removal " + processingGrievances);
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        GrievanceModel grievanceModel = ds.getValue(GrievanceModel.class);
                        if (grievanceModel != null) {
                            if (grievanceModel.getGrievanceStatus() == 1)
                                processingGrievances.add(grievanceModel);
                        }
                    }
                    CustomLogger.getInstance().logDebug( "\nonChildChanged: Arraylist after addition " + processingGrievances);
                    adapter = new RecyclerViewAdapterGrievanceUpdate(processingGrievances, (MainActivity) getActivity(), false);
                    recyclerView.setAdapter(adapter);
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
            }
        };

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                if (processingGrievances.size() > 0) {
                    showEmptyListLayout(false);
                } else
                    showEmptyListLayout(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        String state=Preferences.getInstance().getStaffPref(getContext()).getCircle();
        FireBaseHelper.getInstance().getDataFromFireBase(state,childEventListener, FireBaseHelper.ROOT_GRIEVANCES);
        FireBaseHelper.getInstance().getDataFromFireBase(state,valueEventListener, false, FireBaseHelper.ROOT_GRIEVANCES);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RecyclerViewAdapterGrievanceUpdate.REQUEST_UPDATE: {
                if (resultCode == Activity.RESULT_OK) {
                    //refresh();
                    init();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

    }

}