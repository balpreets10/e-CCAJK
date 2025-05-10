package com.mycca.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.reflect.TypeToken;
import com.mycca.R;
import com.mycca.adapter.RecyclerViewAdapterSavedModels;
import com.mycca.models.GrievanceModel;
import com.mycca.models.InspectionModel;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.Helper;
import com.mycca.tools.IOHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class SavedModelsListFragment<T> extends Fragment {

    RecyclerView recyclerView;
    RecyclerViewAdapterSavedModels adapterSavedModels;
    ArrayList<GrievanceModel> grievanceArrayList;
    ArrayList<InspectionModel> inspectionArrayList;
    String filename;
    Activity activity;

    public SavedModelsListFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_model_list, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_saved_lists);
        activity = getActivity();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            filename = bundle.getString("FileName");
        }
        getSavedModels(filename);
    }

    private void getSavedModels(String filename) {
        IOHelper.getInstance().readFromFile(activity, filename, null, jsonObject -> {
            if (jsonObject != null) {
                CustomLogger.getInstance().logDebug("File contents: " + jsonObject, CustomLogger.Mask.SAVED_MODELS_LIST_FRAGMENT);
                if (filename.equals(IOHelper.GRIEVANCES)) {
                    Type collectionType = new TypeToken<ArrayList<GrievanceModel>>() {
                    }.getType();
                    grievanceArrayList = Helper.getInstance().getCollectionFromJson((String) jsonObject, collectionType);
                    CustomLogger.getInstance().logDebug("arraylist=" + grievanceArrayList, CustomLogger.Mask.SAVED_MODELS_LIST_FRAGMENT);
                    adapterSavedModels = new RecyclerViewAdapterSavedModels(grievanceArrayList, (AppCompatActivity) activity);
                } else {
                    Type collectionType = new TypeToken<ArrayList<InspectionModel>>() {
                    }.getType();
                    inspectionArrayList = Helper.getInstance().getCollectionFromJson((String) jsonObject, collectionType);
                    CustomLogger.getInstance().logDebug("arraylist=" + inspectionArrayList, CustomLogger.Mask.SAVED_MODELS_LIST_FRAGMENT);
                    adapterSavedModels = new RecyclerViewAdapterSavedModels(inspectionArrayList, (AppCompatActivity) activity);
                }
                recyclerView.setAdapter(adapterSavedModels);
                recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            } else {
                CustomLogger.getInstance().logDebug("No data in file", CustomLogger.Mask.SAVED_MODELS_LIST_FRAGMENT);
                //TODO
                //show no models saved message
            }
        });
    }

}
