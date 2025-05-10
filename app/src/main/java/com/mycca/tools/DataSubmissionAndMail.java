package com.mycca.tools;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataSubmissionAndMail {

    public static final String SUBMIT="submit";
    public static final String UPDATE="update";

    private static DataSubmissionAndMail _instance;

    private DataSubmissionAndMail() {
        _instance = this;
    }

    public static DataSubmissionAndMail getInstance() {
        if (_instance == null) {
            return new DataSubmissionAndMail();
        } else {
            return _instance;
        }
    }

    public void uploadImagesToServer(ArrayList<Uri> firebaseImageURLs, String code, String uploadType,VolleyHelper volleyHelper) {
        CustomLogger.getInstance().logDebug("uploadImagesToServer: Starting Upload");
        String url = Helper.getInstance().getAPIUrl() + "uploadImage.php/";
        for (int i = 0; i < firebaseImageURLs.size(); i++) {

            CustomLogger.getInstance().logDebug("uploadAllImagesToServer: Current = " + i);

            Map<String, String> params = new HashMap<>();
            params.put("folder",uploadType);
            params.put("pensionerCode", code);
            params.put("image", firebaseImageURLs.get(i).toString());
            params.put("imageName", "image-" + i);
            params.put("imageCount", i + "");
            if (volleyHelper.countRequestsInFlight("upload_image-" + i) == 0)
                volleyHelper.makeStringRequest(url, "upload_image-" + i, params);

        }
    }

    public void sendMail(Map<String, String> hashMap, String tag, VolleyHelper volleyHelper,String url) {
        if (volleyHelper.countRequestsInFlight(tag) == 0)
            volleyHelper.makeStringRequest(url, tag, hashMap);
        CustomLogger.getInstance().logDebug( "sendFinalMail: ");
    }


}
