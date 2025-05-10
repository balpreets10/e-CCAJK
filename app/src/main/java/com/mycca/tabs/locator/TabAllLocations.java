package com.mycca.tabs.locator;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mycca.R;
import com.mycca.activity.MainActivity;
import com.mycca.adapter.RecyclerViewAdapterHotspotLocation;
import com.mycca.listeners.ClickListener;
import com.mycca.listeners.RecyclerViewTouchListeners;
import com.mycca.models.LocationModel;
import com.mycca.providers.LocationDataProvider;
import com.mycca.tools.CustomLogger;

import java.util.ArrayList;

public class TabAllLocations extends Fragment {

    String TAG = "All Locations";
    Button search, sort;
    RecyclerView recyclerView;
    RecyclerViewAdapterHotspotLocation adapter;
    String[] locations;
    ArrayList<LocationModel> stateLocations = new ArrayList<>();
    ArrayList<LocationModel> filteredLocations = new ArrayList<>();
    MainActivity activity;

    String locatorType;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_all_locations, container, false);
        if (getArguments() != null)
            locatorType = getArguments().getString("Locator");
        CustomLogger.getInstance().logDebug("onCreateView: locator type = " + locatorType);
        init(view);
        //        if (Preferences.getInstance().getBooleanPref(getContext(), Preferences.PREF_HELP_LOCATOR)) {
//            showTutorial();
//            Preferences.getInstance().setBooleanPref(getContext(), Preferences.PREF_HELP_LOCATOR, false);
//        }
        return view;
    }

    private void init(View view) {

        activity = (MainActivity) getActivity();
        /*radioGroup = view.findViewById(R.id.search);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonName) {

                    ShowSearchByNameDialog();

                } else if (checkedId == R.id.radioButtonState) {

                    ShowSearchByStateDialog();
                }
            }
        });*/

        search = view.findViewById(R.id.btn_search_loc);
        search.setOnClickListener(v -> ShowSearchByNameDialog());

        sort = view.findViewById(R.id.button_sort_loc);
        sort.setOnClickListener(v -> ShowSortDialog());

        stateLocations = LocationDataProvider.getInstance().getLocationModelArrayList(locatorType);
        adapter = new RecyclerViewAdapterHotspotLocation(stateLocations);

        if (stateLocations != null) {
            locations = new String[stateLocations.size()];
            int i = 0;
            for (LocationModel locationModel : stateLocations) {
                locations[i] = locationModel.getLocationName();
                i++;
            }
        }
        else
            locations=new String[0];

        recyclerView = view.findViewById(R.id.recyclerview_locations);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.getItemAnimator().setAddDuration(1000);


        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListeners(getContext(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //int pos = rv.getChildAdapterPosition(child);
                LocationModel location = stateLocations.get(position);
                Uri uri = Uri.parse("geo:0,0?q=" + (location.getLatitude() + "," + location.getLongitude() + "(" + Uri.encode(location.getLocationName()) + ")"));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    private void ShowSortDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sort_locations, (ViewGroup) getView(), false);
        final RadioGroup radioGroupSort = viewInflated.findViewById(R.id.radio_group_sort);
        builder.setView(viewInflated);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            if (radioGroupSort.getCheckedRadioButtonId() == R.id.rBName) {
                LocationDataProvider.getInstance().sortByName(stateLocations);
                //adapter.notifyDataSetChanged();
            }
            if (radioGroupSort.getCheckedRadioButtonId() == R.id.rBDistrict) {
                LocationDataProvider.getInstance().sortByDistrict(stateLocations);
                //adapter.notifyDataSetChanged();
            }
            adapter = new RecyclerViewAdapterHotspotLocation(stateLocations);
            recyclerView.setAdapter(adapter);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void ShowSearchByNameDialog() {

        //radioGroup.clearCheck();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search_by_name, (ViewGroup) getView(), false);

        final AutoCompleteTextView input = viewInflated.findViewById(R.id.input);
        input.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_drawable_location, 0, 0, 0);
        ArrayAdapter<String> actAdapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_dropdown_item_1line, locations);
        input.setAdapter(actAdapter);

        builder.setView(viewInflated);
        builder.setPositiveButton(R.string.ok, (dialog, which) -> {
            String name = input.getText().toString();
            FilterLocationsByName(name);
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void FilterLocationsByName(String name) {
        filteredLocations = new ArrayList<>();
        for (LocationModel locationModel : stateLocations) {
            if (locationModel.getLocationName().equals(name))
                filteredLocations.add(locationModel);
        }
        if (filteredLocations.size() == 0) {
            Toast.makeText(getContext(), getString(R.string.no_locations_found), Toast.LENGTH_SHORT).show();
            adapter = new RecyclerViewAdapterHotspotLocation(stateLocations);
        } else {
            adapter = new RecyclerViewAdapterHotspotLocation(filteredLocations);
        }
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setupGridLayout(true);
        }
    }

    private void setupGridLayout(boolean multiColumn) {
        if (multiColumn) {
            GridLayoutManager manager = new GridLayoutManager(this.getContext(), 2);
            recyclerView.setLayoutManager(manager);
        } else {
            GridLayoutManager manager = new GridLayoutManager(this.getContext(), 1);
            recyclerView.setLayoutManager(manager);
        }
    }

}

//    private void showTutorial() {
//
//        final FancyShowCaseView fancyShowCaseView2 = new FancyShowCaseView.Builder(activity)
//                .title("Tap on location to open in Google Maps")
//                .focusOn(recyclerView)
//                .focusCircleRadiusFactor(.6)
//                .titleStyle(R.style.FancyShowCaseDefaultTitleStyle, Gravity.BOTTOM | Gravity.CENTER)
//                .build();
//        final FancyShowCaseView fancyShowCaseView1 = new FancyShowCaseView.Builder(activity)
//                .title("Click to search location")
//                .focusOn(search)
//                .focusShape(FocusShape.ROUNDED_RECTANGLE)
//                .fitSystemWindows(true)
//                .build();
//        final FancyShowCaseView fancyShowCaseView3 = new FancyShowCaseView.Builder(activity)
//                .title("Click to view locations districtwise")
//                .focusOn(sort)
//                .focusShape(FocusShape.ROUNDED_RECTANGLE)
//                .fitSystemWindows(true)
//                .build();
//        final FancyShowCaseView fancyShowCaseView4 = new FancyShowCaseView.Builder(activity)
//                .title("-------->\nSwipe to view nearby hotspots")
//                .focusCircleAtPosition(Resources.getSystem().getDisplayMetrics().widthPixels * 3 / 4, Resources.getSystem().getDisplayMetrics().heightPixels / 6, 150)
//                .build();
//
//        activity.setmQueue(new FancyShowCaseQueue()
//                .add(fancyShowCaseView2)
//                .add(fancyShowCaseView1)
//                .add(fancyShowCaseView3)
//                .add(fancyShowCaseView4));
//
//        activity.getmQueue().setCompleteListener(() -> activity.setmQueue(null));
//
//        activity.getmQueue().show();
//    }


/*private void ShowSearchByStateDialog() {
        radioGroup.clearCheck();
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search_by_state, (ViewGroup) getView(), false);

        stateSpinner = viewInflated.findViewById(R.id.spinnerState);
        CustomAdapter statesAdapter = new CustomAdapter(getContext(), FireBaseHelper.getInstance().statelist);
        stateSpinner.setAdapter(statesAdapter);

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Circle state = FireBaseHelper.getInstance().statelist.get(position);
                populateSpinnerDistrict(viewInflated, state.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setView(viewInflated);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FilterLocationsByState(stateSpinner.getSelectedItem(), districtSpinner.getSelectedItem());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }


    private void populateSpinnerDistrict(View view, String stateId) {
        HashSet<String> districts = new HashSet<>();
        districtSpinner = view.findViewById(R.id.spinnerDistrict);
        for (LocationModel locationModel : stateLocations) {
            if (locationModel.getStateID().equals(stateId))
                districts.add(locationModel.getDistrict());
        }
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, new ArrayList<String>(districts));
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(adapterSpinner);
    }




    private void FilterLocationsByState(Object selectedItem, Object selectedItem1) {
        filteredLocations = new ArrayList<>();
        for (LocationModel locationModel : stateLocations) {
            if (locationModel.getStateID().equals(((Circle) selectedItem).getId())) {
                if (locationModel.getDistrict().equals(selectedItem1))
                    filteredLocations.add(locationModel);
            }
        }
        if (filteredLocations.size() == 0) {
            Toast.makeText(getContext(), "No Locations found", Toast.LENGTH_SHORT).show();
            adapter = new RecyclerViewAdapterHotspotLocation(stateLocations);
        } else {
            adapter = new RecyclerViewAdapterHotspotLocation(filteredLocations);
        }
        recyclerView.setAdapter(adapter);
    }
*/