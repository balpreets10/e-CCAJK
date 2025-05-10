package com.mycca.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mycca.R;
import com.mycca.activity.KypUploadActivity;
import com.mycca.custom.MySubmittableFragment;
import com.mycca.enums.State;


public class KYPFragment extends MySubmittableFragment {

    LinearLayout download, submit;
    Activity activity;

    public KYPFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kyp, container, false);
        bindViews(view);
        init();
        return view;
    }

    private void bindViews(View view) {
        download = view.findViewById(R.id.ll_download_kyp);
        submit = view.findViewById(R.id.ll_submit_kyp);
    }

    private void init() {
        activity = getActivity();
        download.setOnClickListener(v -> downloadForm());

        submit.setOnClickListener(v -> startActivity(new Intent(getActivity(), KypUploadActivity.class)));
    }

    private void downloadForm() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                startDowlnoad();
            } else
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            startDowlnoad();
        }
    }

    private void startDowlnoad() {
        String url = "https://firebasestorage.googleapis.com/v0/b/cca-jk.appspot.com/o/AppFiles%2FKYP%2Fkyp.pdf?alt=media&token=7ef8f473-817c-4152-9d8d-3b103af59809";
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Know Your Pensioner");
        request.setTitle("KYP Form");
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "kyp");

        DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            manager.enqueue(request);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            startDowlnoad();
    }


    @Override
    public void updateState(State state) {
        this.state = state;
    }
}