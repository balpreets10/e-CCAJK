package com.mycca.providers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mycca.listeners.DownloadCompleteListener;
import com.mycca.models.Circle;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.IOHelper;
import com.mycca.tools.Preferences;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CircleDataProvider {

    private static CircleDataProvider _instance;
    private Circle[] circles;
    private Circle[] activeCircles;
    private int activeCount;
    private int circleCount;
    private Trace mTrace;

    private CircleDataProvider() {
        _instance = this;
    }

    public static CircleDataProvider getInstance() {
        if (_instance == null) {
            return new CircleDataProvider();
        } else {
            return _instance;
        }
    }

    public Circle[] getActiveCircleData() {
        return activeCircles;
    }

    public Circle[] getCircleData() {
        return circles;
    }

    public Circle getCircleFromCode(String code) {
        for (Circle activeCircle : activeCircles) {
            if (activeCircle.getCode().equals(code))
                return activeCircle;
        }
        return activeCircles[0];
    }

    public void setCircleData(Boolean fromFirebase, Context context, DownloadCompleteListener downloadCompleteListener) {
        String SET_CIRCLES_TRACE = "SET_CIRCLES_trace";
        mTrace = FirebasePerformance.getInstance().newTrace(SET_CIRCLES_TRACE);
        mTrace.start();

        if (fromFirebase) {
            getCircleDataFromFireBase(context, downloadCompleteListener);
        } else {
            IOHelper.getInstance().readFromFile(context, IOHelper.CIRCLES, null,
                    jsonObject -> {
                        Gson gson = new Gson();
                        Type collectionType = new TypeToken<ArrayList<Circle>>() {
                        }.getType();
                        ArrayList<Circle> circleArrayList = gson.fromJson(jsonObject.toString(), collectionType);
                        setArrayLists(circleArrayList,
                                Preferences.getInstance().getIntPref(context, Preferences.PREF_ACTIVE_CIRCLES));
                    });
        }
    }

    private void setArrayLists(ArrayList<Circle> arrayList, int activeCount) {
        int i = 0, j = 0;
        if (arrayList != null) {
            circles = new Circle[arrayList.size()];
            activeCircles = new Circle[activeCount];

            for (Circle circle : arrayList) {
                circles[i++] = circle;
                if (circle.isActive())
                    activeCircles[j++] = circle;
            }
        }
    }

    private void getCircleDataFromFireBase(Context context, DownloadCompleteListener downloadCompleteListener) {

        ArrayList<Circle> circleArrayList = new ArrayList<>();

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CustomLogger.getInstance().logDebug("Got circle data from firebase");
                activeCount = 0;
                circleCount = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    try {
                        Circle circle = ds.getValue(Circle.class);
                        circleArrayList.add(circle);
                        circleCount++;
                        if (circle != null && circle.isActive()) {
                            activeCount++;
                        }
                    } catch (DatabaseException | NullPointerException e) {
                        CustomLogger.getInstance().logDebug(e.getMessage());
                    }
                }

                setArrayLists(circleArrayList, activeCount);

                IOHelper.getInstance().writeToFile(context, new Gson().toJson(circleArrayList),
                        IOHelper.CIRCLES, null,
                        success -> {
                            if (success) {
                                CustomLogger.getInstance().logDebug("Write circle Success..Setting Preferences = " + circleCount + "," + activeCount);
                                Preferences.getInstance().setIntPref(context, Preferences.PREF_CIRCLES, circleCount);
                                Preferences.getInstance().setIntPref(context, Preferences.PREF_ACTIVE_CIRCLES, activeCount);
                                if (downloadCompleteListener != null)
                                    downloadCompleteListener.onDownloadSuccess();
                            } else {
                                CustomLogger.getInstance().logDebug("Write circle Failed");
                                if (downloadCompleteListener != null)
                                    downloadCompleteListener.onDownloadFailure();
                            }
                        });
                mTrace.stop();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (downloadCompleteListener != null)
                    downloadCompleteListener.onDownloadFailure();
                mTrace.stop();
            }
        };

        FireBaseHelper.getInstance().getDataFromFireBase(null, valueEventListener, false, FireBaseHelper.ROOT_CIRCLE_DATA);

    }

}
