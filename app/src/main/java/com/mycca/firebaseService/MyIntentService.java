package com.mycca.firebaseService;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class MyIntentService extends IntentService {

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Toast.makeText(getBaseContext(),"Service",Toast.LENGTH_LONG).show();
            //Preferences.getInstance().setBooleanPref(getApplicationContext(),Preferences.PREF_TEST,false);
        }
    }

}