package com.mycca.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mycca.activity.MainActivity;
import com.mycca.R;

public class AboutUsFragment extends Fragment {
    ImageView imageViewLogo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about_us, container, false);
        imageViewLogo = view.findViewById(R.id.img_about_logo);
        imageViewLogo.setOnClickListener(v -> {
            MainActivity mainActivity = (MainActivity) getActivity();
            BrowserFragment fragment = new BrowserFragment();
            Bundle bundle = new Bundle();
            bundle.putString("url", "http://gamingdronzz.com");
            if (mainActivity != null) {
                mainActivity.showFragment(getString(R.string.gaming_dronzz), fragment, bundle);
            }

        });
        return view;
    }
}
