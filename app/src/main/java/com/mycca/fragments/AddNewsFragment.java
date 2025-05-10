package com.mycca.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mycca.R;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.models.NewsModel;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.Helper;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Preferences;

import java.util.Date;
import java.util.HashMap;

public class AddNewsFragment extends Fragment {

    TextInputEditText textTitle, textDescription;
    Button add;
    ProgressDialog progressDialog;
    NewsModel newsModel;
    String json = null;

    public AddNewsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_news, container, false);
        if (getArguments() != null)
            json = getArguments().getString("News");
        bindViews(view);
        init();
        return view;
    }

    private void bindViews(View view) {
        textTitle = view.findViewById(R.id.text_add_news_headline);
        textDescription = view.findViewById(R.id.text_add_news_description);
        add = view.findViewById(R.id.button_add_news);
        progressDialog = Helper.getInstance().getProgressWindow(getActivity(), getString(R.string.please_wait));
    }

    private void init() {
        if (json != null) {
            newsModel = (NewsModel) Helper.getInstance().getObjectFromJson(json, NewsModel.class);
            textTitle.setText(newsModel.getHeadline());
            textDescription.setText(newsModel.getDescription());
        }
        add.setOnClickListener(v -> {
            if (checkInput()) {
                progressDialog.show();
                checkConnection();
            }
        });
    }

    private boolean checkInput() {

        if (textTitle.getText().toString().trim().isEmpty()) {
            textTitle.setError(getString(R.string.headline_req));
            textTitle.requestFocus();
            return false;
        } else if (textDescription.getText().toString().trim().isEmpty()) {
            textDescription.setError(getString(R.string.description_req));
            textDescription.requestFocus();
            return false;
        } else {
            return true;
        }

    }

    private void checkConnection() {
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                CustomLogger.getInstance().logDebug("version checked= " + Helper.versionChecked, CustomLogger.Mask.ADD_NEWS_FRAGMENT);
                if (Helper.versionChecked) {
                    addNewsToFireBase();
                } else{
                    ValueEventListener valueEventListener= new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            try {
                                long value = (long) dataSnapshot.getValue();
                                if (Helper.getInstance().onLatestVersion(value, getActivity()))
                                    addNewsToFireBase();
                            } catch (Exception e) {
                                Helper.getInstance().showMaintenanceDialog(getActivity(), null);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Helper.getInstance().showMaintenanceDialog(getActivity(),null);
                        }
                    };
                    FireBaseHelper.getInstance().getDataFromFireBase(null,valueEventListener,true, FireBaseHelper.ROOT_INITIAL_CHECKS,FireBaseHelper.ROOT_APP_VERSION);
                }


            }

            @Override
            public void OnConnectionNotAvailable() {
                progressDialog.dismiss();
                Helper.getInstance().noInternetDialog(getActivity());
            }
        });
        connectionUtility.checkConnectionAvailability();

    }

    public void addNewsToFireBase() {
        Task<Void> task;

        if (json == null) {
            newsModel = new NewsModel(
                    new Date(),
                    new Date(),
                    textTitle.getText().toString(),
                    textDescription.getText().toString(),
                    Preferences.getInstance().getStaffPref(getContext()).getCircle(),
                    null,
                    Preferences.getInstance().getStaffPref(getContext()).getId(),
                    Preferences.getInstance().getStaffPref(getContext()).getId());
            task = FireBaseHelper.getInstance().pushOnFireBase(newsModel,
                    FireBaseHelper.ROOT_NEWS);
        } else {
            newsModel.setHeadline(textTitle.getText().toString());
            newsModel.setDescription(textDescription.getText().toString());
            HashMap<String, Object> result = new HashMap<>();
            result.put("headline", newsModel.getHeadline().trim());
            result.put("description", newsModel.getDescription().trim());
            result.put("updatedBy",Preferences.getInstance().getStaffPref(getContext()).getId());
            result.put("dateUpdated",new Date());
            task = FireBaseHelper.getInstance().updateData(null,newsModel.getKey(), result, FireBaseHelper.ROOT_NEWS);

        }

        task.addOnCompleteListener(task1 -> {
            progressDialog.dismiss();
            if (task1.isSuccessful()) {
                Helper.getInstance().showMessage(getActivity(), "", getString(R.string.news_added),FancyAlertDialogType.SUCCESS);
                if (json != null) json = null;
            } else {
                Helper.getInstance().showMaintenanceDialog(getActivity(),null);
            }
        });
    }
}
