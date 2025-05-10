package com.mycca.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.tabs.updateGrievance.TabResolved;
import com.mycca.tabs.updateGrievance.TabSubmitted;
import com.mycca.tabs.updateGrievance.TabUnderProcess;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.Helper;

import java.util.List;


public class UpdateGrievanceFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    RelativeLayout relativeLayoutNoInternet;
    LinearLayout linearLayoutTab;
    ImageButton imageButtonRefresh;
    ProgressDialog progressDialog;
    MainActivity activity;

    public final static int INT_UPDATE_GRIEVANCE_TAB_ITEMS = 3;
    String TAG = "UpdateGrievanceFragment";

    public UpdateGrievanceFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update, container, false);
        bindViews(view);
        showNoInternetConnectionLayout(true);
        init();
        return view;
    }

    private void showNoInternetConnectionLayout(boolean show) {
        if (show) {
            relativeLayoutNoInternet.setVisibility(View.VISIBLE);
            linearLayoutTab.setVisibility(View.GONE);
        } else {
            relativeLayoutNoInternet.setVisibility(View.GONE);
            linearLayoutTab.setVisibility(View.VISIBLE);
        }
    }

    void bindViews(View view) {
        tabLayout = view.findViewById(R.id.tab_update_grievances);
        viewPager = view.findViewById(R.id.viewpager_update_grievance);
        relativeLayoutNoInternet = view.findViewById(R.id.layout_no_internet_update_grievance_fragment);
        linearLayoutTab = view.findViewById(R.id.linear_layout_update_grievance_fragment);
        imageButtonRefresh = view.findViewById(R.id.image_btn_refresh_update_grievance_fragment);
        imageButtonRefresh.setOnClickListener(v -> init());
    }

    private void init() {
        activity = (MainActivity) getActivity();
        progressDialog = Helper.getInstance().getProgressWindow(activity, getString(R.string.please_wait));
        progressDialog.show();
        checkConnection();
    }

    private void checkConnection() {
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                setTabLayout();
                showNoInternetConnectionLayout(false);
            }

            @Override
            public void OnConnectionNotAvailable() {
                progressDialog.dismiss();
                showNoInternetConnectionLayout(true);
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    private void setTabLayout() {
        progressDialog.dismiss();
        final MyAdapter adapter = new MyAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);
        //        if (Preferences.getInstance().getBooleanPref(getContext(), Preferences.PREF_HELP_UPDATE)) {
//            showTutorial();
//            Preferences.getInstance().setBooleanPref(getContext(), Preferences.PREF_HELP_UPDATE, false);
//        }
    }


    class MyAdapter extends FragmentPagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new TabSubmitted();
                case 1:
                    return new TabUnderProcess();
                case 2:
                    return new TabResolved();
            }
            return null;
        }

        @Override
        public int getCount() {
            return INT_UPDATE_GRIEVANCE_TAB_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return getString(R.string.submitted).toUpperCase();
                case 1:
                    return getString(R.string.under_process).toUpperCase();
                case 2:
                    return getString(R.string.resolved).toUpperCase();

            }
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> allFragments = getChildFragmentManager().getFragments();

        for (Fragment frag : allFragments) {
            CustomLogger.getInstance().logDebug("onRequestPermissionsResult: " + frag.toString());
            frag.onActivityResult(requestCode, resultCode, data);
        }
    }
}


//    private void showTutorial() {
//
//        final FancyShowCaseView fancyShowCaseView1 = new FancyShowCaseView.Builder(activity)
//                .title("These are submitted grievances")
//                .focusCircleAtPosition(Resources.getSystem().getDisplayMetrics().widthPixels / 6, Resources.getSystem().getDisplayMetrics().heightPixels / 6, 150)
//                .build();
//
//        final FancyShowCaseView fancyShowCaseView2 = new FancyShowCaseView.Builder(activity)
//                .title("-------->\nSwipe to view Grievances Under process")
//                .focusCircleAtPosition(Resources.getSystem().getDisplayMetrics().widthPixels / 2, Resources.getSystem().getDisplayMetrics().heightPixels / 6, 150)
//                .build();
//
//        final FancyShowCaseView fancyShowCaseView3 = new FancyShowCaseView.Builder(activity)
//                .title("-------->\nSwipe again to view Resolved Grievances")
//                .focusCircleAtPosition(Resources.getSystem().getDisplayMetrics().widthPixels * 5 / 6, Resources.getSystem().getDisplayMetrics().heightPixels / 6, 150)
//                .build();
//
//        activity.setmQueue(new FancyShowCaseQueue()
//                .add(fancyShowCaseView1)
//                .add(fancyShowCaseView2)
//                .add(fancyShowCaseView3));
//
//        activity.getmQueue().setCompleteListener(() -> activity.setmQueue(null));
//
//        activity.getmQueue().show();
//    }