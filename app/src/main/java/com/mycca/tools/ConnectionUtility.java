package com.mycca.tools;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.mycca.interfaces.IConnectivityProcessor;
import com.mycca.listeners.OnConnectionAvailableListener;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ConnectionUtility implements IConnectivityProcessor {
    private OnConnectionAvailableListener onConnectionAvailableListener;


    final String TAG = "ConnectionUtility";
    public final static String _2G = "2G";
    public final static String _3G = "3G";
    public final static String _4G = "4G";
    public final static String _UNKNOWN = "Unknown";

    public ConnectionUtility(OnConnectionAvailableListener onConnectionAvailableListener) {
        this.onConnectionAvailableListener = onConnectionAvailableListener;
    }

    @Override
    public void checkConnectionAvailability() {
        String customURL = Helper.getInstance().getConnectionCheckURL();
        MyTask task = new MyTask();
        task.execute(customURL);
    }

    public static String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType;
        if (mTelephonyManager != null) {
            networkType = mTelephonyManager.getNetworkType();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return _2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return _3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return _4G;
                default:
                    return _UNKNOWN;
            }
        }
        return _UNKNOWN;
    }

    private class MyTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    HttpsURLConnection.setFollowRedirects(false);
                    HttpsURLConnection con = (HttpsURLConnection) new URL(params[0]).openConnection();
                    con.setRequestMethod("HEAD");
                    //System.out.println(con.getResponseCode());
                    int responseresult = con.getResponseCode();
                    return responseresult == HttpsURLConnection.HTTP_OK;
                } else {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            boolean bResponse = result;
            if (bResponse) {
                CustomLogger.getInstance().logDebug("URl Exists");
                if (onConnectionAvailableListener != null) {
                    onConnectionAvailableListener.OnConnectionAvailable();
                }
            } else {
                CustomLogger.getInstance().logDebug("URl Does not Exist");
                if (onConnectionAvailableListener != null) {
                    onConnectionAvailableListener.OnConnectionNotAvailable();
                }
            }
        }
    }
}
