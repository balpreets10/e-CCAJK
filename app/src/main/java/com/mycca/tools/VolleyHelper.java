package com.mycca.tools;

import android.content.Context;
import android.widget.ImageView;

import com.android.volley.Cache.Entry;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.mycca.app.AppController;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class VolleyHelper {

    public final String SUCCESS = "success";

    private VolleyResponse delegate;
    private ErrorListener errorListener = new VolleyErrorListener();
    private Listener<JSONArray> jsonArrayResponseListener = new JsonArrayResponseListener();
    private Listener<JSONObject> jsonObjectResponseListener = new JsonObjectResponseListener();
    private Listener<String> stringResponseListener = new StringResponseListener();

    public interface VolleyResponse {
        void onError(VolleyError volleyError);

        void onResponse(String str);
    }

    class StringResponseListener implements Listener<String> {
        StringResponseListener() {
        }

        public void onResponse(String response) {
            VolleyHelper.this.delegate.onResponse(response);
        }
    }

    class JsonObjectResponseListener implements Listener<JSONObject> {
        JsonObjectResponseListener() {
        }

        public void onResponse(JSONObject response) {
        }
    }

    class JsonArrayResponseListener implements Listener<JSONArray> {
        JsonArrayResponseListener() {
        }

        public void onResponse(JSONArray response) {
        }
    }

    class VolleyErrorListener implements ErrorListener {
        VolleyErrorListener() {
        }

        public void onErrorResponse(VolleyError error) {
            if (error != null) {
                VolleyHelper.this.delegate.onError(error);
            } else {
                CustomLogger.getInstance().logDebug("onErrorResponse: Null Error Object ", CustomLogger.Mask.VOLLEY);
            }
        }
    }

    private class CountRequestsInFlight implements RequestFilter {
        int count = 0;
        Object tag;

        CountRequestsInFlight(Object tag) {
            this.tag = tag;
        }

        public boolean apply(Request<?> request) {
            if (request.getTag().equals(this.tag)) {
                this.count++;
            }
            return false;
        }

        public int getCount() {
            return this.count;
        }
    }

    public VolleyHelper(VolleyResponse delegate, Context context) {
        this.delegate = delegate;
    }

    public void makeStringRequest(String url, String TAG) {
        StringRequest strReq = new StringRequest(1, url, this.stringResponseListener, this.errorListener);
        setShouldCache(strReq, true);
        AppController.getInstance().addToRequestQueue(strReq, TAG);
    }

    public void makeStringRequest(String url, String TAG, Map<String, String> params) {
        CustomLogger.getInstance().logDebug("makeStringRequest: " + url, CustomLogger.Mask.VOLLEY);
        final Map<String, String> map = params;
        StringRequest strReq = new StringRequest(Request.Method.POST, url, this.stringResponseListener, this.errorListener) {
            protected Map<String, String> getParams() {
                CustomLogger.getInstance().logDebug("get Params: ", CustomLogger.Mask.VOLLEY);
                return map;
            }
        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        setShouldCache(strReq, true);
        CustomLogger.getInstance().logDebug("Adding to request Queue", CustomLogger.Mask.VOLLEY);
        AppController.getInstance().addToRequestQueue(strReq, TAG);
    }

    public void makeJsonRequest(String url, String TAG) {
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(1, url, null, this.jsonObjectResponseListener, this.errorListener), TAG);
    }

    public void makeJsonRequest(String url, String TAG, Map<String, String> params) {
        final Map<String, String> map = params;
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(1, url, null, this.jsonObjectResponseListener, this.errorListener) {
            protected Map<String, String> getParams() {
                return map;
            }
        }, TAG);
    }

    public void makeJsonRequest(String url, String TAG, JSONObject notification_data, Map<String, String> header) {

        CustomLogger.getInstance().logDebug("makeStringRequest: " + url, CustomLogger.Mask.VOLLEY);
        final Map<String, String> headerMap = header;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, notification_data, this.jsonObjectResponseListener, this.errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                return headerMap;
            }
        };
        AppController.getInstance().addToRequestQueue(request, TAG);
    }

    public void makeJsonArrayRequest(String url, String TAG) {
        AppController.getInstance().addToRequestQueue(new JsonArrayRequest(1, url, null, this.jsonArrayResponseListener, this.errorListener), TAG);
    }

    public void makeJsonArrayRequest(String url, String TAG, Map<String, String> params) {
        final Map<String, String> map = params;
        AppController.getInstance().addToRequestQueue(new JsonArrayRequest(1, url, null, this.jsonArrayResponseListener, this.errorListener) {
            protected Map<String, String> getParams() {
                return map;
            }
        }, TAG);
    }

    public NetworkImageView loadImageInNetworkImageView(NetworkImageView networkImageView, String URL) {
        networkImageView.setImageUrl(URL, AppController.getInstance().getImageLoader());
        return networkImageView;
    }

    public ImageView loadImageInImageView(final ImageView imageView, String URL_IMAGE) {
        AppController.getInstance().getImageLoader().get(URL_IMAGE, new ImageListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }

            public void onResponse(ImageContainer response, boolean arg1) {
                if (response.getBitmap() != null) {
                    imageView.setImageBitmap(response.getBitmap());
                }
            }
        });
        return imageView;
    }

    public ImageView loadImageInImageViewWithLoaders(ImageView imageView, String URL_IMAGE) {
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        return imageView;
    }

    public String getCachedData(String url) {
        Entry entry = AppController.getInstance().getRequestQueue().getCache().get(url);
        if (entry == null) {
            return null;
        }
        try {
            return new String(entry.data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String invalidateCachedData(String url) {
        AppController.getInstance().getRequestQueue().getCache().invalidate(url, true);
        return "";
    }

    private void setShouldCache(StringRequest stringRequest, boolean should) {
        stringRequest.setShouldCache(false);
    }

    private void setShouldCache(JsonObjectRequest jsonObjectRequest, boolean should) {
        jsonObjectRequest.setShouldCache(false);
    }

    private void setShouldCache(JsonArrayRequest jsonArrayRequest, boolean should) {
        jsonArrayRequest.setShouldCache(false);
    }

    public int countRequestsInFlight(String tag) {
        RequestQueue queue = AppController.getInstance().getRequestQueue();
        RequestFilter inFlight = new CountRequestsInFlight(tag);
        queue.cancelAll(inFlight);
        return 0;
        //return inFlight.getCount();
    }

    public void removeCachedURL(String url) {
        AppController.getInstance().getRequestQueue().getCache().remove(url);
    }

    public void removeCache() {
        AppController.getInstance().getRequestQueue().getCache().clear();
    }

    public void cancelRequest(String TAG) {
        AppController.getInstance().getRequestQueue().cancelAll(TAG);
    }
}
