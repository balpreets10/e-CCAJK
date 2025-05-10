package com.mycca.notification;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mycca.notification.Constants.KEY_DATA;
import static com.mycca.notification.Constants.KEY_TO;


public class FirebaseNotificationHelper {

    private String mReceiverFireBaseToken;
    private String serverApiKey;
    private String jsonObject;
    private String url;
    private FireBaseCallBack callBack;

    private FirebaseNotificationHelper(String serverApiKey) {
        this.serverApiKey = serverApiKey;
    }

    public static FirebaseNotificationHelper initialize(String serverApiKey) {
        return new FirebaseNotificationHelper(serverApiKey);
    }

    public FirebaseNotificationHelper receiverFirebaseToken(String receiverFirebaseToken) {
        mReceiverFireBaseToken = receiverFirebaseToken;
        return this;
    }


    public FirebaseNotificationHelper jsonBody(String object) {
        this.jsonObject = object;
        return this;
    }

    public FirebaseNotificationHelper setCallBack(FireBaseCallBack callBack) {
        this.callBack = callBack;
        return this;
    }

    public void send() {

        new NetworkCall(callBack).execute(getValidJsonBody().toString(), serverApiKey);
    }

    private JSONObject getValidJsonBody() {
        JSONObject jsonObjectBody = new JSONObject();
        try {

            jsonObjectBody.put(KEY_TO, mReceiverFireBaseToken);
            JSONObject jsonObjectData = new JSONObject(jsonObject);
            jsonObjectBody.put(KEY_DATA, jsonObjectData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObjectBody;
    }

}
