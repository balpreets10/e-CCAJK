package com.mycca.providers;

import com.mycca.models.LocationModel;
import com.mycca.tools.FireBaseHelper;

import java.util.ArrayList;
import java.util.Collections;

public class LocationDataProvider {

    private static LocationDataProvider _instance;
    private ArrayList<LocationModel> hotspotLocationModelArrayList;
    private ArrayList<LocationModel> gpLocationModelArrayList;

    private LocationDataProvider() {
        _instance = this;
    }

    public static LocationDataProvider getInstance() {
        if (_instance == null) {
            return new LocationDataProvider();
        } else {
            return _instance;
        }
    }

    public ArrayList<LocationModel> getLocationModelArrayList(String type) {
        if (type.equals(FireBaseHelper.ROOT_GP)) {
            return gpLocationModelArrayList;
        } else {
            return hotspotLocationModelArrayList;
        }
    }

    public void setLocationModelArrayList(String type, ArrayList<LocationModel> arrayList) {
        sortByName(arrayList);
        if (type.equals(FireBaseHelper.ROOT_GP)) {
            gpLocationModelArrayList = arrayList;
        } else {
            hotspotLocationModelArrayList = arrayList;
        }
    }

    public void sortByName(ArrayList<LocationModel> arrayList) {
        Collections.sort(arrayList, (o1, o2) -> (o1.getLocationName().toLowerCase().compareTo(o2.getLocationName().toLowerCase())));
    }

    public void sortByDistrict(ArrayList<LocationModel> arrayList) {
        Collections.sort(arrayList, (o1, o2) -> (o1.getDistrict().toLowerCase().compareTo(o2.getDistrict().toLowerCase())));
    }
}
