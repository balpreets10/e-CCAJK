package com.mycca.fragments;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.models.LocationModel;
import com.mycca.providers.LocationDataProvider;
import com.mycca.tabs.locator.TabAllLocations;
import com.mycca.tabs.locator.TabNearby;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.Helper;
import com.mycca.tools.IOHelper;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Preferences;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocatorFragment extends Fragment {

    public TabLayout tabLayout;
    public ViewPager viewPager;
    ProgressDialog progressDialog;
    public ArrayList<LocationModel> locationModelArrayList = new ArrayList<>();
    public final static int INT_LOCATOR_TAB_ITEMS = 2;
    boolean fileExists = false;
    String locatorType;
    TextView textViewLocatorInfo;
    RelativeLayout relativeLayoutNoInternet;
    LinearLayout linearLayoutTab;
    ImageButton imageButtonRefresh;
    MainActivity activity;

    public LocatorFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locator_layout, container, false);

        if (getArguments() != null)
            locatorType = getArguments().getString("Locator");
        bindViews(view);
        init();
        return view;
    }

    private void bindViews(View view) {
        tabLayout = view.findViewById(R.id.tab_locator);
        viewPager = view.findViewById(R.id.viewpager_locator);
        relativeLayoutNoInternet = view.findViewById(R.id.layout_no_internet_locator_fragment);
        linearLayoutTab = view.findViewById(R.id.linear_layout_locator_fragment);
        textViewLocatorInfo = view.findViewById(R.id.textview_locator_info);
        imageButtonRefresh = view.findViewById(R.id.image_btn_refresh_tab_all);
    }

    private void init() {

        activity = (MainActivity) getActivity();
        manageNoLocationLayout(true);

        progressDialog = Helper.getInstance().getProgressWindow(activity, getString(R.string.please_wait));
        imageButtonRefresh.setOnClickListener(v -> checkConnection());
        String noInternet = textViewLocatorInfo.getText() + "\n" + getString(R.string.refresh);
        textViewLocatorInfo.setText(noInternet);

        getLocationsFromLocalStorage();
    }

    private void manageNoLocationLayout(boolean show) {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        if (show) {
            relativeLayoutNoInternet.setVisibility(View.VISIBLE);
            linearLayoutTab.setVisibility(View.GONE);
        } else {
            relativeLayoutNoInternet.setVisibility(View.GONE);
            linearLayoutTab.setVisibility(View.VISIBLE);
        }
    }

    private void getLocationsFromLocalStorage() {

        progressDialog.show();
        IOHelper.getInstance().readFromFile(activity, locatorType,
                Preferences.getInstance().getCirclePref(activity).getCode(),
                jsonObject -> {
                    if (jsonObject == null)
                        fileExists = false;
                    else {
                        fileExists = true;
                        String json = String.valueOf(jsonObject);
                        try {
                            Type collectionType = new TypeToken<ArrayList<LocationModel>>() {
                            }.getType();
                            locationModelArrayList = new Gson().fromJson(json, collectionType);
                        } catch (JsonParseException jpe) {
                            locationModelArrayList = null;
                        }
                    }
                    checkConnection();
                });

    }

    private void checkConnection() {
        if (progressDialog != null && !progressDialog.isShowing())
            progressDialog.show();
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                if (!fileExists) {
                    fetchLocationsFromFirebase();
                } else {
                    String networkClass = ConnectionUtility.getNetworkClass(activity);
                    if (networkClass.equals(ConnectionUtility._2G)) {
                        setTabLayout();
                    } else {
                        checkNewLocationsInFirebase();
                    }
                }
            }

            @Override
            public void OnConnectionNotAvailable() {
                if (!fileExists) {
                    manageNoLocationLayout(true);
                } else {
                    setTabLayout();
                }

            }
        });
        connectionUtility.checkConnectionAvailability();
    }


    private void checkNewLocationsInFirebase() {
        String node;
        if (locatorType.equals(FireBaseHelper.ROOT_GP))
            node = FireBaseHelper.ROOT_GP_COUNT;
        else node = FireBaseHelper.ROOT_WIFI_COUNT;

        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    long firebaseCount = (long) dataSnapshot.getValue();
                    if (locationModelArrayList.size() == firebaseCount) {
                        setTabLayout();
                    } else {
                        CustomLogger.getInstance().logDebug("init: new locations in firebase", CustomLogger.Mask.LOCATOR_FRAGMENT);
                        fetchLocationsFromFirebase();
                    }
                } else {
                    setTabLayout();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                CustomLogger.getInstance().logDebug("onCancelled: " + databaseError.getMessage(), CustomLogger.Mask.LOCATOR_FRAGMENT);
                setTabLayout();
            }
        };
        FireBaseHelper.getInstance().getDataFromFireBase(Preferences.getInstance().getCirclePref(activity).getCode(),
                vel, false, node);
    }

    private void fetchLocationsFromFirebase() {

        locationModelArrayList = new ArrayList<>();
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        CustomLogger.getInstance().logDebug("onChildAdded: " + dataSnapshot.getKey(), CustomLogger.Mask.LOCATOR_FRAGMENT);
                        LocationModel location = dataSnapshot.getValue(LocationModel.class);
                        locationModelArrayList.add(location);
                    } catch (DatabaseException dbe) {
                        dbe.printStackTrace();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {

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
                CustomLogger.getInstance().logDebug("onDataChange: got locations from firebase", CustomLogger.Mask.LOCATOR_FRAGMENT);
                setTabLayout();
                if (locationModelArrayList != null)
                    addLocationsToLocalStorage();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        FireBaseHelper.getInstance().getDataFromFireBase(Preferences.getInstance().getCirclePref(activity).getCode(),
                childEventListener,
                locatorType);
        FireBaseHelper.getInstance().getDataFromFireBase(Preferences.getInstance().getCirclePref(activity).getCode(),
                valueEventListener, true, locatorType);
    }

    private void addLocationsToLocalStorage() {
        try {
            String jsonObject = Helper.getInstance().getJsonFromObject(locationModelArrayList);
            CustomLogger.getInstance().logDebug("Json: " + jsonObject, CustomLogger.Mask.LOCATOR_FRAGMENT);
            CustomLogger.getInstance().logDebug("adding LocationsToLocalStorage: ", CustomLogger.Mask.LOCATOR_FRAGMENT);
            IOHelper.getInstance().writeToFile(activity, jsonObject, locatorType,
                    Preferences.getInstance().getCirclePref(activity).getCode(),
                    success -> {
                    });
        } catch (JsonParseException jpe) {
            jpe.printStackTrace();
        }
    }

    private void setTabLayout() {
        setData();
        manageNoLocationLayout(false);
        final MyAdapter adapter = new MyAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                CustomLogger.getInstance().logDebug("onPageSelected: selected = " + position, CustomLogger.Mask.LOCATOR_FRAGMENT);

                Fragment fragment = adapter.getCurrentFragment();
                if (fragment instanceof TabNearby) {
                    ((TabNearby) fragment).startLocationProcess();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setData() {
        LocationDataProvider.getInstance().setLocationModelArrayList(locatorType, locationModelArrayList);
    }

    class MyAdapter extends FragmentPagerAdapter {
        private Fragment mCurrentFragment;

        Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    TabAllLocations tabAllLocations = new TabAllLocations();
                    tabAllLocations.setArguments(getArguments());
                    return tabAllLocations;
                case 1:
                    TabNearby tabNearby = new TabNearby();
                    tabNearby.setArguments(getArguments());
                    return tabNearby;

            }
            return null;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((Fragment) object);
            }
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public int getCount() {
            return INT_LOCATOR_TAB_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 1:
                    return getString(R.string.nearby);
                case 0:
                    return getString(R.string.all_locations);
            }
            return null;
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> allFragments = getChildFragmentManager().getFragments();

        for (Fragment frag : allFragments) {
            CustomLogger.getInstance().logDebug("onRequestPermissionsResult: " + frag.toString(), CustomLogger.Mask.LOCATOR_FRAGMENT);
            frag.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> allFragments = getChildFragmentManager().getFragments();

        for (Fragment frag : allFragments) {
            CustomLogger.getInstance().logDebug("onActivityResult: " + frag.toString(), CustomLogger.Mask.LOCATOR_FRAGMENT);
            frag.onActivityResult(requestCode, resultCode, data);
        }


    }

}
