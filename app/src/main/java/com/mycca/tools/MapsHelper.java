package com.mycca.tools;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mycca.R;
import com.mycca.models.LocationModel;

import java.util.ArrayList;

public class MapsHelper {

    private Context context;

    public MapsHelper(Context context) {
        this.context = context;
    }


    public void AnimateCamera(ArrayList<LocationModel> allLocations, int zoom, GoogleMap mMap, Location mLastLocation, int value) {

        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        mMap.clear();
        Circle circle = mMap.addCircle(new CircleOptions().center(latLng).radius(getRadius(value) * 1000).strokeColor(Color.RED));
        circle.setVisible(true);
        getZoomLevel(circle);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        CustomLogger.getInstance().logDebug("Maps Animation Done");

        ArrayList<LocationModel> filteredLocations = filterLocations(allLocations, value, mLastLocation);
        if (filteredLocations.size() == 0) {
            Toast.makeText(context, context.getString(R.string.increase_radius), Toast.LENGTH_LONG).show();
            return;
        }

        for (LocationModel locationModel : filteredLocations) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(locationModel.getLatitude(), locationModel.getLongitude()))
                    .title(locationModel.getLocationName()));
        }
    }


    private ArrayList<LocationModel> filterLocations(ArrayList<LocationModel> locationModels, int value, Location mLastLocation) {
        if (locationModels == null) {
            return null;
        }
        ArrayList<LocationModel> filteredLocations = new ArrayList<>();
        int length = locationModels.size();
        float[] results = new float[1];
        double radius = getRadius(value) * 1000;

        for (int i = 0; i < length; i++) {
            LocationModel locationModel = locationModels.get(i);
            Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(), locationModel.getLatitude(), locationModel.getLongitude(), results);
            if (results[0] <= radius) {
                filteredLocations.add(locationModel);
            }
        }
        return filteredLocations;
    }


    public double getRadius(int value) {
        double radius = Math.pow(value + 5, 2);
        CustomLogger.getInstance().logDebug("Radius = " + radius);
        return radius;
    }


    private int getZoomLevel(Circle circle) {
        int zoomLevel = 1;
        if (circle != null) {
            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }
}
