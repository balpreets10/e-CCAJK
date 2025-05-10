package com.mycca.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.mycca.adapter.RecyclerViewAdapterContacts;
import com.mycca.custom.FancyAlertDialog.FancyAlertDialogType;
import com.mycca.custom.Progress.ProgressDialog;
import com.mycca.listeners.OnConnectionAvailableListener;
import com.mycca.models.Contact;
import com.mycca.tools.ConnectionUtility;
import com.mycca.tools.CustomLogger;
import com.mycca.tools.FireBaseHelper;
import com.mycca.tools.Helper;
import com.mycca.tools.IOHelper;
import com.mycca.tools.Preferences;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ContactUsFragment extends Fragment {

    AppCompatTextView textViewOfficeAddress, textviewHeadingOfficeAddress, textviewContactPersonHeading;
    LinearLayout officeAddressLayout;
    AppCompatButton compatButtonLocateOnMap;
    RecyclerView recyclerView;
    RecyclerViewAdapterContacts adapterContacts;
    ArrayList<Contact> contactArrayList;
    ProgressDialog progressDialog;
    boolean isTab, fileExists = false;
    MainActivity activity;

    public ContactUsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);
        isTab = Helper.getInstance().isTab(this.getContext());
        bindViews(view);
        if (isTab)
            init(true);
        else {
            init(false);
        }
        //        if (Preferences.getInstance().getBooleanPref(getContext(), Preferences.PREF_HELP_CONTACT)) {
//            showTutorial();
//            Preferences.getInstance().setBooleanPref(getContext(), Preferences.PREF_HELP_CONTACT, false);
//        }
        return view;

    }

    private void bindViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerview_contacts);
        textViewOfficeAddress = view.findViewById(R.id.textview_office_address);
        officeAddressLayout = view.findViewById(R.id.linear_layout_office_Address_Area);
        textviewHeadingOfficeAddress = view.findViewById(R.id.textview_heading_office_address);
        textviewContactPersonHeading = view.findViewById(R.id.textview_contact_person_heading);
        textviewContactPersonHeading.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_person_black_24dp, 0, 0, 0);
        compatButtonLocateOnMap = view.findViewById(R.id.button_locate_on_map);
        compatButtonLocateOnMap.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_drawable_location, 0, 0, 0);

    }

    private void init(boolean isMultiColumn) {

        activity = (MainActivity) getActivity();
        progressDialog = Helper.getInstance().getProgressWindow(activity, getString(R.string.please_wait));
        textViewOfficeAddress.setText(getGeneralText());

        compatButtonLocateOnMap.setOnClickListener(v -> {

            String location = Preferences.getInstance().getStringPref(activity, Preferences.PREF_OFFICE_COORDINATES);
            String label = Preferences.getInstance().getStringPref(activity, Preferences.PREF_OFFICE_LABEL);
            CustomLogger.getInstance().logDebug("location: " + location + " label: " + label, CustomLogger.Mask.CONTACT_US_FRAGMENT);
            if (label != null) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + location + label);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(getContext(), getString(R.string.no_map_app), Toast.LENGTH_SHORT).show();
                }
            } else {
                Helper.getInstance().showMessage(activity, getString(R.string.office_location_n_a),
                        getString(R.string.app_name), FancyAlertDialogType.WARNING);
            }

        });

        if (!isMultiColumn) {
            textviewHeadingOfficeAddress.setOnClickListener(v -> ManageOfficeAddress());
        }
        ManageOfficeAddress();

        contactArrayList = new ArrayList<>();
        getContactArrayList();

    }

    private void ManageOfficeAddress() {
        if (officeAddressLayout.getVisibility() == View.GONE) {
            officeAddressLayout.setVisibility(View.VISIBLE);
            textviewHeadingOfficeAddress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_office, 0, R.drawable.ic_arrow_drop_up_black_24dp, 0);
        } else {
            officeAddressLayout.setVisibility(View.GONE);
            textviewHeadingOfficeAddress.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_office, 0, R.drawable.ic_arrow_drop_down_black_24dp, 0);
        }
    }

    private String getGeneralText() {
        return Preferences.getInstance().getStringPref(activity, Preferences.PREF_OFFICE_ADDRESS);
    }

    private void setAdapter() {
        progressDialog.dismiss();
        CustomLogger.getInstance().logDebug("Setting Adapter", CustomLogger.Mask.CONTACT_US_FRAGMENT);
        adapterContacts = new RecyclerViewAdapterContacts(contactArrayList, activity);
        recyclerView.setAdapter(adapterContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    private void getContactArrayList() {

        getContactsFromLocalStorage();
        ConnectionUtility connectionUtility = new ConnectionUtility(new OnConnectionAvailableListener() {
            @Override
            public void OnConnectionAvailable() {
                progressDialog.show();
                if (!fileExists) {
                    getContactsFromFirebase();
                } else {
                    String networkClass = ConnectionUtility.getNetworkClass(activity);
                    if (networkClass.equals(ConnectionUtility._2G)) {
                        setAdapter();
                    } else {
                        checkNewContactsInFirebase();
                    }
                }
            }

            @Override
            public void OnConnectionNotAvailable() {
                setAdapter();
            }
        });
        connectionUtility.checkConnectionAvailability();
    }

    private void getContactsFromLocalStorage() {
        IOHelper.getInstance().readFromFile(activity, IOHelper.CONTACTS,
                Preferences.getInstance().getCirclePref(activity).getCode(),
                jsonObject -> {
                    if (jsonObject == null)
                        fileExists = false;
                    else {
                        fileExists = true;
                        String json = String.valueOf(jsonObject);
                        try {
                            CustomLogger.getInstance().logDebug(json, CustomLogger.Mask.CONTACT_US_FRAGMENT);
                            Type collectionType = new TypeToken<ArrayList<Contact>>() {
                            }.getType();
                            contactArrayList = new Gson().fromJson(json, collectionType);
                            CustomLogger.getInstance().logDebug(contactArrayList.toString(), CustomLogger.Mask.CONTACT_US_FRAGMENT);

                        } catch (JsonParseException jpe) {
                            jpe.printStackTrace();
                            contactArrayList = null;
                        }
                    }
                });
    }

    private void getContactsFromFirebase() {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    try {
                        CustomLogger.getInstance().logDebug("onChildAdded: " + dataSnapshot.getKey(), CustomLogger.Mask.CONTACT_US_FRAGMENT);
                        Contact contact = dataSnapshot.getValue(Contact.class);
                        contactArrayList.add(contact);
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
                CustomLogger.getInstance().logDebug("got contacts from firebase", CustomLogger.Mask.CONTACT_US_FRAGMENT);
                setAdapter();
                if (contactArrayList.size() > 0) {
                    addContactsToLocalStorage(contactArrayList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        FireBaseHelper.getInstance().getDataFromFireBase(Preferences.getInstance().getCirclePref(activity).getCode(),
                childEventListener,
                FireBaseHelper.ROOT_CONTACTS);
        FireBaseHelper.getInstance().getDataFromFireBase(Preferences.getInstance().getCirclePref(activity).getCode(),
                valueEventListener, true, FireBaseHelper.ROOT_CONTACTS);

    }

    private void addContactsToLocalStorage(ArrayList<Contact> contactArrayList) {
        try {
            String jsonObject = Helper.getInstance().getJsonFromObject(contactArrayList);
            CustomLogger.getInstance().logDebug("Json: " + jsonObject, CustomLogger.Mask.CONTACT_US_FRAGMENT);
            CustomLogger.getInstance().logDebug("adding ContactsToLocalStorage: " + contactArrayList.size(), CustomLogger.Mask.CONTACT_US_FRAGMENT);
            IOHelper.getInstance().writeToFile(activity, jsonObject, IOHelper.CONTACTS,
                    Preferences.getInstance().getCirclePref(activity).getCode(),
                    success -> {
                    });
        } catch (JsonParseException jpe) {
            jpe.printStackTrace();
        }
    }

    private void checkNewContactsInFirebase() {
        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    long firebaseCount = (long) dataSnapshot.getValue();
                    if (contactArrayList.size() == firebaseCount) {
                        CustomLogger.getInstance().logDebug("no new contacts", CustomLogger.Mask.CONTACT_US_FRAGMENT);
                        setAdapter();
                    } else {
                        CustomLogger.getInstance().logDebug("new contacts found", CustomLogger.Mask.CONTACT_US_FRAGMENT);
                        getContactsFromFirebase();
                    }
                } catch (DatabaseException dbe) {
                    dbe.printStackTrace();
                    setAdapter();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                CustomLogger.getInstance().logDebug("onCancelled: " + databaseError.getMessage(), CustomLogger.Mask.CONTACT_US_FRAGMENT);
                setAdapter();
            }
        };
        FireBaseHelper.getInstance().getDataFromFireBase(Preferences.getInstance().getCirclePref(activity).getCode(),
                vel, false, FireBaseHelper.ROOT_CONTACTS_COUNT);

    }


}


//    private void showTutorial() {
//
//        contactArrayList.get(0).setExpanded(true);
//        adapterContacts.notifyItemChanged(0);
//
//        final FancyShowCaseView fancyShowCaseView1 = new FancyShowCaseView.Builder(activity)
//                .title("Touch to open office address")
//                .focusOn(textviewHeadingOfficeAddress)
//                .focusShape(FocusShape.ROUNDED_RECTANGLE)
//                .build();
//
//        final FancyShowCaseView fancyShowCaseView2 = new FancyShowCaseView.Builder(activity)
//                .title("Tap on any contact to open or close contact information")
//                .focusOn(recyclerView)
//                .focusCircleRadiusFactor(.8)
//                .titleStyle(R.style.FancyShowCaseDefaultTitleStyle, Gravity.TOP | Gravity.CENTER)
//                .build();
//
//        final FancyShowCaseView fancyShowCaseView3 = new FancyShowCaseView.Builder(activity)
//                .title("Tap on phone numbers to make call or on email to compose email")
//                .focusOn(recyclerView)
//                .focusCircleRadiusFactor(.6)
//                .titleStyle(R.style.FancyShowCaseDefaultTitleStyle, Gravity.TOP | Gravity.CENTER)
//                .build();
//
//        activity.setmQueue(new FancyShowCaseQueue()
//                .add(fancyShowCaseView1)
//                .add(fancyShowCaseView2)
//                .add(fancyShowCaseView3));
//
//        activity.getmQueue().setCompleteListener(() -> {
//            activity.setmQueue(null);
//            contactArrayList.get(0).setExpanded(false);
//            adapterContacts.notifyItemChanged(0);
//        });
//
//        activity.getmQueue().show();
//    }