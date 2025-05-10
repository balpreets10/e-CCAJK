package com.mycca.tools;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mycca.listeners.DownloadCompleteListener;
import com.mycca.models.Circle;
import com.mycca.models.NewsModel;
import com.mycca.models.SelectedImageModel;

import java.util.HashMap;

public class FireBaseHelper {

    private static FireBaseHelper mInstance;

    public final static String ROOT_FCM_KEY = "FCMServerKey";
    public final static String ROOT_ACTIVE = "Active";
    public final static String ROOT_SLIDER = "Slider Data";
    public final static String ROOT_CIRCLE_DATA = "Circle Data";
    public final static String ROOT_CIRCLE_COUNT = "Circle Data Count";
    public final static String ROOT_ACTIVE_COUNT = "Circle Data Count Active";
    public final static String ROOT_TOKEN = "Tokens";
    public final static String ROOT_STAFF = "Staff Data";
    public final static String ROOT_PASSWORD = "password";
    public final static String ROOT_CONTACTS = "Contact Persons";
    public final static String ROOT_CONTACTS_COUNT = "Contact Persons Count";
    public final static String ROOT_WIFI = "Wifi Locations";
    public final static String ROOT_WIFI_COUNT = "Wifi Locations Count";
    public final static String ROOT_GP = "GP Locations";
    public final static String ROOT_GP_COUNT = "GP Locations Count";
    public final static String ROOT_SUGGESTIONS = "Suggestions";
    public final static String ROOT_BY_USER = "By User";
    public final static String ROOT_BY_STAFF = "By Staff";
    public final static String ROOT_ADHAAR = "Aadhaar";
    public final static String ROOT_PAN = "Pan";
    public final static String ROOT_LIFE = "Life Certificate";
    public final static String ROOT_RE_MARRIAGE = "Re-Marriage Certificate";
    public final static String ROOT_RE_EMPLOYMENT = "Re-Employment Certificate";
    public final static String ROOT_KYP = "KYP";
    public final static String ROOT_INSPECTION = "Inspection";
    public final static String ROOT_PENSION_STATUS = "Pension File Status";
    public final static String ROOT_INITIAL_CHECKS = "Initial checks";
    private final static String ROOT_OTHER_DATA = "Other Data";
    private final static String ROOT_WEBSITE = "Website";
    private final static String ROOT_OFFICE_ADDRESS = "Office Address";
    private final static String ROOT_OFFICE_COORDINATES = "Office Coordinates";
    private final static String ROOT_GENERAL = "General";


    /*Release*/
    public final static String ROOT_APP_VERSION = "Latest Version";
    public final static String ROOT_NEWS = "Latest News";
    public final static String ROOT_GRIEVANCES = "Grievances";
    public final static String ROOT_REF_NUMBERS = "Reference Numbers";
    private final static String ROOT_REF_COUNT = "Reference Number Count";

    /*Debug*/
    //    public final static String ROOT_APP_VERSION = "Debug Latest Version";
//    public final static String ROOT_NEWS = "Debug Latest News";
//    public final static String ROOT_GRIEVANCES = "Debug Grievances";
//    public final static String ROOT_REF_COUNT = "Debug Reference Number Count";
//    public final static String ROOT_REF_NUMBERS = "Debug Reference Numbers";


    private FireBaseHelper() {
        mInstance = this;
    }

    public static FireBaseHelper getInstance() {
        if (mInstance == null) {
            return new FireBaseHelper();
        } else {
            return mInstance;
        }
    }



    /*-------------------------FIREBASE REFERENCES------------------------*/

    public FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    private DatabaseReference getDatabaseReference(String circleCode) {
        DatabaseReference databaseReference;
        if (circleCode != null) {
            String url = "https://cca-" + circleCode + ".firebaseio.com/";
            databaseReference = FirebaseDatabase.getInstance(url).getReference();

        } else
            databaseReference = FirebaseDatabase.getInstance().getReference();
        return databaseReference;
    }

    private StorageReference getStorageReference(String circleCode) {
        StorageReference storageReference;
        if (circleCode != null) {
            String url = "gs://cca-" + circleCode;
            storageReference = FirebaseStorage.getInstance(url).getReference();
        } else
            storageReference = FirebaseStorage.getInstance().getReference();
        return storageReference;
    }



    /*-------------------------FIREBASE CALLS------------------------------*/

    public void addTokenOnFireBase() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
            if (getAuth().getCurrentUser() != null) {
                getDatabaseReference(null).child(ROOT_TOKEN).
                        child(getAuth().getCurrentUser().getUid()).
                        setValue(instanceIdResult.getToken()).
                        addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                CustomLogger.getInstance().logDebug("Token added");
                            }
                        });
            }
        });
    }

    public void getReferenceNumber(Transaction.Handler handler, String circleCode) {
        CustomLogger.getInstance().logDebug("getReferenceNumber: ");
        DatabaseReference dbref = getDatabaseReference(circleCode);
        dbref = dbref.child(ROOT_GENERAL).child(ROOT_REF_COUNT);
        dbref.runTransaction(handler);
    }

    public Task<Void> uploadDataToFireBase(String circleCode, Object model, String... params) {
        Task<Void> task;
        DatabaseReference databaseReference = getDatabaseReference(circleCode);

        if (!params[0].equals(ROOT_STAFF) && circleCode != null)
            databaseReference = databaseReference.child(ROOT_GENERAL);
        for (String key : params) {
            CustomLogger.getInstance().logDebug("\nkey" + key);
            databaseReference = databaseReference.child(key);
        }
        task = databaseReference.setValue(model);
        return task;
    }

    public Task<Void> pushOnFireBase(Object model, String root) {

        Task<Void> task = null;
        switch (root) {
            case ROOT_NEWS:
                NewsModel newsModel = (NewsModel) model;
                if (newsModel.getKey() == null) {
                    CustomLogger.getInstance().logDebug("news key null");
                    String key = getDatabaseReference(null).child(root).push().getKey();
                    newsModel.setKey(key);
                    task = getDatabaseReference(null).child(root).child(key).setValue(newsModel);
                }
                break;
            case ROOT_SUGGESTIONS:
                task = getDatabaseReference(null).child(root).push().setValue(model);
                break;
        }
        return task;
    }

    public Task<Void> updateData(String circleCode, String key, HashMap<String, Object> hashMap, String... params) {
        DatabaseReference databaseReference = getDatabaseReference(circleCode);
        if (!params[0].equals(ROOT_STAFF) && circleCode != null)
            databaseReference = databaseReference.child(ROOT_GENERAL);

        Task<Void> task;
        for (String param : params) {
            databaseReference = databaseReference.child(param);
        }
        task = databaseReference.child(key).updateChildren(hashMap);
        return task;
    }

    public Task<Void> removeData(String circleCode, String... params) {
        DatabaseReference databaseReference = getDatabaseReference(circleCode);
        if (!params[0].equals(ROOT_STAFF) && circleCode != null)
            databaseReference = databaseReference.child(ROOT_GENERAL);

        Task<Void> task;
        for (String param : params) {
            databaseReference = databaseReference.child(param);
        }
        task = databaseReference.removeValue();
        return task;
    }

    public void getDataFromFireBase(String circleCode, ChildEventListener childEventListener, String... params) {

        DatabaseReference dbref = getDatabaseReference(circleCode);
        if (!params[0].equals(ROOT_STAFF) && circleCode != null)
            dbref = dbref.child(ROOT_GENERAL);
        for (String key : params) {
            CustomLogger.getInstance().logDebug("\nkey : " + key);
            dbref = dbref.child(key);
        }
        dbref.addChildEventListener(childEventListener);
    }

    public void getDataFromFireBase(String circleCode, ValueEventListener valueEventListener, boolean singleValueEvent, String... params) {

        DatabaseReference dbref = getDatabaseReference(circleCode);
        if (!params[0].equals(ROOT_STAFF) && circleCode != null)
            dbref = dbref.child(ROOT_GENERAL);
        for (String key : params) {
            CustomLogger.getInstance().logDebug("\nkey : " + key);
            dbref = dbref.child(key);
        }
        if (singleValueEvent)
            dbref.addListenerForSingleValueEvent(valueEventListener);
        else
            dbref.addValueEventListener(valueEventListener);
    }

    public UploadTask uploadFiles(String circleCode, SelectedImageModel imageFile, boolean multiple, int count, String... params) {
        StorageReference sRef;
        StringBuilder sb = new StringBuilder();
        for (String param : params)
            sb.append(param).append("/");
        if (multiple) {
            sRef = getStorageReference(circleCode).child(sb + "File" + count);
        } else {
            sRef = getStorageReference(circleCode).child(sb.toString());
        }
        return sRef.putFile(imageFile.getImageURI());
    }

    public Task<Uri> getFileFromStorage(String circleCode, String path) {
        return getStorageReference(circleCode).child(path).getDownloadUrl();
    }

    public void getOtherStateData(Context context, DownloadCompleteListener downloadCompleteListener) {

        Circle circle = Preferences.getInstance().getCirclePref(context);
        CustomLogger.getInstance().logDebug("Getting other data");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {

                    Preferences.getInstance().setStringPref(context, Preferences.PREF_OFFICE_ADDRESS,
                            (String) dataSnapshot.child(ROOT_OFFICE_ADDRESS).getValue());
                    Preferences.getInstance().setStringPref(context, Preferences.PREF_WEBSITE,
                            (String) dataSnapshot.child(ROOT_WEBSITE).getValue());
                    Preferences.getInstance().setStringPref(context, Preferences.PREF_OFFICE_LABEL,
                            (String) dataSnapshot.child(ROOT_OFFICE_COORDINATES).child("label").getValue());
                    Preferences.getInstance().setStringPref(context, Preferences.PREF_OFFICE_COORDINATES,
                            dataSnapshot.child(ROOT_OFFICE_COORDINATES).child("latitude").getValue() + "," + dataSnapshot.child(ROOT_OFFICE_COORDINATES).child("longitude").getValue());

                    if (downloadCompleteListener != null)
                        downloadCompleteListener.onDownloadSuccess();

                } catch (DatabaseException | NullPointerException e) {
                    if (downloadCompleteListener != null)
                        downloadCompleteListener.onDownloadFailure();
                    CustomLogger.getInstance().logDebug(e.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                downloadCompleteListener.onDownloadFailure();
            }
        };

        getDataFromFireBase(circle.getCode(), valueEventListener, true, ROOT_OTHER_DATA);
    }

}
