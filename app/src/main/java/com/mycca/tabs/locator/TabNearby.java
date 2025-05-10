package com.mycca.tabs.locator;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.mycca.R;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.IndicatorSeekBar.IndicatorSeekBar;
import com.mycca.custom.IndicatorSeekBar.IndicatorSeekBarType;
import com.mycca.custom.IndicatorSeekBar.IndicatorType;
import com.mycca.custom.IndicatorSeekBar.TickType;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.models.LocationModel;
import com.mycca.providers.LocationDataProvider;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.Helper;
import com.mycca.tools.MapsHelper;
import com.mycca.tools.MyLocationManager;

import java.util.ArrayList;

import static com.mycca.tools.MyLocationManager.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static com.mycca.tools.MyLocationManager.LOCATION_REQUEST_CODE;

public class TabNearby extends Fragment implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {


    private int seekBarValue;
    private final String TAG = "Nearby";
    String locatorType;
    private ArrayList<LocationModel> locationModels = new ArrayList<>();

    TextView kilometres;
    IndicatorSeekBar seekBar;
    ProgressDialog progressDialog;
    public MyLocationManager locationManager;
    MapsHelper mapsHelper;
    ImageButton buttonRefresh;
    RelativeLayout relativeLayoutNoLocation;

    private GoogleMap mMap;
    private Location mLastLocation;
    Activity activity;
    View mapView;
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            CustomLogger.getInstance().logVerbose( "Updating My Location");
            progressDialog.dismiss();
            for (Location location : locationResult.getLocations()) {
                mLastLocation = location;
                placeMarkerOnMyLocation(location);
                locationManager.cleanUp();
                manageNoLocationLayout(false);
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_nearby_locations, container, false);
        if (getArguments() != null)
            locatorType = getArguments().getString("Locator");
        CustomLogger.getInstance().logDebug( "onCreateView: tabnearby created");
        bindViews(view);
        init();
        return view;
    }

    private void init() {
        CustomLogger.getInstance().logDebug( "init: tabnearby init");

        activity = getActivity();
        progressDialog = Helper.getInstance().getProgressWindow(activity, "");

        buttonRefresh.setOnClickListener(v -> {
            CustomLogger.getInstance().logDebug( "onClick: managing");
            //locationManager.ManageLocation();
            startLocationProcess();
        });
        manageNoLocationLayout(true);
        locationManager = new MyLocationManager(this, mLocationCallback);

        locationModels = LocationDataProvider.getInstance().getLocationModelArrayList(locatorType);
        seekBar.getBuilder()
                .setMax(3)
                .setMin(0)
                .setProgress(0)
                .setSeekBarType(IndicatorSeekBarType.DISCRETE_TICKS)
                .setTickType(TickType.OVAL)
                .setTickNum(1)
                .setBackgroundTrackSize(2)//dp size
                .setProgressTrackSize(3)//dp size
                .setIndicatorType(IndicatorType.CIRCULAR_BUBBLE)
                .setIndicatorColor(getResources().getColor(R.color.colorAccentLight))
                .build();

        seekBar.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                seekBarValue = seekBar.getProgress();
                String within = String.format(getString(R.string.within), String.valueOf(mapsHelper.getRadius(seekBarValue)));
                kilometres.setText(within);
                if (mLastLocation != null) {
                    mapsHelper.AnimateCamera(locationModels, getZoomValue(seekBarValue), mMap, mLastLocation, seekBarValue);
                } else {
                    Task<LocationSettingsResponse> task = locationManager.ManageLocation();
                    if (task != null) {
                        task.addOnCompleteListener(task1 -> {
                            CustomLogger.getInstance().logVerbose( "On Task Complete");
                            if (task1.isSuccessful()) {
                                CustomLogger.getInstance().logVerbose( "Task is Successful");
                                locationManager.requestLocationUpdates(mMap);
                                manageNoLocationLayout(false);


                            } else {
                                CustomLogger.getInstance().logVerbose( "Task is not Successful");
                            }
                        });
                        task.addOnSuccessListener(activity, locationSettingsResponse -> {
                            CustomLogger.getInstance().logVerbose( "On Task Success");
                            // All location settings are satisfied. The client can initialize
                            // location requests here.
                            // ...

                        });

                        task.addOnFailureListener(activity, e -> {
                            CustomLogger.getInstance().logVerbose( "On Task Failed");
                            if (e instanceof ResolvableApiException) {
                                locationManager.onLocationAcccessRequestFailure(e);
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog.

                            }
                        });
                    }
                }
            }
        });

    }

    private void bindViews(View view) {
        CustomLogger.getInstance().logDebug( "bindViews: ");
        kilometres = view.findViewById(R.id.textview_range);
        relativeLayoutNoLocation = view.findViewById(R.id.layout_no_location);
        mapsHelper = new MapsHelper(view.getContext());
        buttonRefresh = view.findViewById(R.id.image_btn_refresh);
        seekBar = view.findViewById(R.id.seekBar);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

    }

    private void manageNoLocationLayout(boolean show) {
        if (show)
            relativeLayoutNoLocation.setVisibility(View.VISIBLE);
        else
            relativeLayoutNoLocation.setVisibility(View.GONE);
    }

    public void startLocationProcess() {
        Task<LocationSettingsResponse> task = locationManager.ManageLocation();
        if (task != null) {
            task.addOnCompleteListener(task1 -> {
                CustomLogger.getInstance().logVerbose( "On Task Complete");
                if (task1.isSuccessful()) {
                    CustomLogger.getInstance().logVerbose( "Task is Successful");
                    progressDialog.setMessage(getString(R.string.getting_coordinates));
                    progressDialog.show();
                    locationManager.requestLocationUpdates(mMap);
                    manageNoLocationLayout(false);
                } else {
                    CustomLogger.getInstance().logVerbose( "Task is not Successful");
                }
            });
            task.addOnSuccessListener(activity, locationSettingsResponse -> {
                CustomLogger.getInstance().logVerbose( "On Task Success");
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...

            });

            task.addOnFailureListener(activity, e -> {
                CustomLogger.getInstance().logVerbose( "On Task Failed");
                if (e instanceof ResolvableApiException) {
                    locationManager.onLocationAcccessRequestFailure(e);
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.

                }
            });
        }


        if (mLastLocation != null) {
            mapsHelper.AnimateCamera(locationModels, 0, mMap, mLastLocation, seekBarValue);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        setMyMap(googleMap);


    }

    private void setMyMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        CustomLogger.getInstance().logVerbose( "Maps Set");

        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("4"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        // position on right bottom


        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        rlp.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
        rlp.addRule(RelativeLayout.ALIGN_END, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlp.setMargins(20, 25, 0, 0);
    }

    private void placeMarkerOnMyLocation(Location location) {
        CustomLogger.getInstance().logVerbose( "Location: " + location.getLatitude() + " " + location.getLongitude());
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(getString(R.string.current_pos));
        mapsHelper.AnimateCamera(locationModels, 12, mMap, mLastLocation, seekBarValue);
        CustomLogger.getInstance().logVerbose( "Animating through Callback ");
    }

    private int getZoomValue(int seekBarValue) {
        switch (seekBarValue) {
            case 1:
                return 10;
            case 2:
            case 3:
                return 9;
            case 4:
            case 5:
                return 8;

        }
        return 1;
        // return 12 - seekBarValue;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        if (mLastLocation == null) {
            Toast.makeText(this.activity, getString(R.string.enable_location), Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this.activity, getString(R.string.you_are_here), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        for (String s : permissions) {
            CustomLogger.getInstance().logVerbose( "Permissions = " + s);
        }

        CustomLogger.getInstance().logDebug( "onRequestPermissionsResult: rc = " + requestCode + " l rc = " + LOCATION_REQUEST_CODE + "result = " + grantResults[0]);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Task<LocationSettingsResponse> task = locationManager.ManageLocation();
                    if (task != null) {
                        final Task<LocationSettingsResponse> locationSettingsResponseTask = task.addOnCompleteListener(task1 -> {
                            CustomLogger.getInstance().logVerbose( "On Task Complete");
                            if (task1.isSuccessful()) {
                                CustomLogger.getInstance().logVerbose( "Task is Successful");
                                progressDialog.setMessage(getString(R.string.getting_coordinates));
                                progressDialog.show();
                                locationManager.requestLocationUpdates(mMap);
                                manageNoLocationLayout(false);

                            } else {
                                CustomLogger.getInstance().logVerbose( "Task is not Successful");
                            }
                        });
                        task.addOnSuccessListener(activity, locationSettingsResponse -> {
                            CustomLogger.getInstance().logVerbose( "On Task Success");
                            // All location settings are satisfied. The client can initialize
                            // location requests here.
                            // ...

                        });

                        task.addOnFailureListener(activity, e -> {
                            CustomLogger.getInstance().logVerbose( "On Task Failed");
                            if (e instanceof ResolvableApiException) {
                                locationManager.onLocationAcccessRequestFailure(e);
                                // Location settings are not satisfied, but this can be fixed
                                // by showing the user a dialog.

                            }
                        });
                        mMap.setMyLocationEnabled(true);
                    } else {
                        locationManager.ShowDialogOnPermissionDenied(getString(R.string.location_denied));
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLogger.getInstance().logDebug( "Result Code = " + Integer.toString(resultCode) + "Request code = " + requestCode + " connection code = " + CONNECTION_FAILURE_RESOLUTION_REQUEST);
        //final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);

        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        CustomLogger.getInstance().logVerbose( "Resolution success");
                        locationManager.requestLocationUpdates(mMap);
                        manageNoLocationLayout(false);
                        progressDialog.setMessage(getString(R.string.turning_on_location));
                        progressDialog.show();
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        CustomLogger.getInstance().logVerbose( "Resolution denied");
                        Helper.getInstance().showFancyAlertDialog(activity,
                                getString(R.string.loc_off),
                                getString(R.string.nearby),
                                getString(R.string.ok),
                                null,
                                null,
                                null,
                                FancyAlertDialogType.WARNING);

                        break;
                    }
                    default: {
                        CustomLogger.getInstance().logVerbose( "User unable to do anything");
                        break;
                    }
                }
                break;
        }
    }
}

