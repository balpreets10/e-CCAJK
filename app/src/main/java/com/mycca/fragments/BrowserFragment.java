package com.mycca.fragments;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.mycca.R;
import com.mycca.tools.CustomLogger;


public class BrowserFragment extends Fragment {

    String TAG = "browser";
    WebView webView;
    ProgressBar progressBar;
    FrameLayout frameLayoutProgress;
    String url;
    ActionBar actionBar;
    private boolean hasStopped = false;
    ObjectAnimator progressAnimator;
//    int previousProgress = 0;

    public BrowserFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);
        setHasOptionsMenu(true);
        init(view);
        setupWebview();
        hasStopped = false;
        webView.loadUrl(url);
        return view;
    }

    private void init(View view) {
        Bundle args = getArguments();
        if (getActivity() != null) {
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        }
        frameLayoutProgress = view.findViewById(R.id.fl_progress);
        frameLayoutProgress.setVisibility(View.GONE);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setMax(1000);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        webView = view.findViewById(R.id.webview_cca);
        if (args != null) {
            url = args.getString("url");
        }
//        progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_link:
                if (frameLayoutProgress.getVisibility() == View.GONE)
                    webView.reload();
                else {

                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_browser, menu);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebview() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);

        webView.setWebViewClient(new WebViewClient() {

            @RequiresApi(Build.VERSION_CODES.O)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                CustomLogger.getInstance().logDebug("UrlO", url, CustomLogger.Mask.BROWSER_FRAGMENT);
                view.loadUrl(request.getUrl().toString());
                return true;

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    CustomLogger.getInstance().logDebug("Url", url, CustomLogger.Mask.BROWSER_FRAGMENT);
                    view.loadUrl(url);
                }
                return true;
            }

            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                setSubtitle(getString(R.string.some_error));
                frameLayoutProgress.setVisibility(View.GONE);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                setSubtitle(getString(R.string.loading));
                frameLayoutProgress.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
//                progressAnimator.setIntValues(50, progressBar.getProgress());
//                progressAnimator.setDuration(300);
//                progressAnimator.setInterpolator(new LinearInterpolator());
//                progressAnimator.start();
//                previousProgress = 50;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                setSubtitle(view.getTitle());
                frameLayoutProgress.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);

//                progressAnimator.setIntValues(1000, previousProgress);
//                progressAnimator.setDuration(300);
//                progressAnimator.setInterpolator(new LinearInterpolator());
//                progressAnimator.start();
//                progressAnimator.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        frameLayoutProgress.setVisibility(View.GONE);
//                        super.onAnimationEnd(animation);
//                    }
//                });
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
//                progressAnimator.setIntValues(newProgress * 10, previousProgress);
//                progressAnimator.setDuration(100);
//                progressAnimator.setInterpolator(new LinearInterpolator());
//                progressAnimator.start();
//                previousProgress = newProgress * 10;
                progressBar.setProgress(newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (!TextUtils.isEmpty(title)) {
                    setSubtitle(title);
                }
            }
        });

    }

    void setSubtitle(String subtitle) {
        if (!hasStopped)
            actionBar.setSubtitle(Html.fromHtml("<font color='#000000'>" + subtitle + "</font>"));
    }

    public boolean canGoBack() {
        return webView.canGoBack();
    }

    public void goBack() {
        webView.goBack();
        Log.v(TAG, "Going back");
    }

    public void stopLoading() {
        webView.stopLoading();

        hasStopped = true;
    }

    @Override
    public void onPause() {
        CustomLogger.getInstance().logDebug(TAG, "onPause: ", CustomLogger.Mask.BROWSER_FRAGMENT);
        super.onPause();
    }

    @Override
    public void onDetach() {
        CustomLogger.getInstance().logDebug(TAG, "onDetach: ", CustomLogger.Mask.BROWSER_FRAGMENT);
        super.onDetach();
    }

    @Override
    public void onStop() {
        CustomLogger.getInstance().logDebug(TAG, "onStop: ", CustomLogger.Mask.BROWSER_FRAGMENT);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        CustomLogger.getInstance().logDebug(TAG, "onDestroy: ", CustomLogger.Mask.BROWSER_FRAGMENT);
        stopLoading();
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        CustomLogger.getInstance().logDebug(TAG, "onDestroyView: ", CustomLogger.Mask.BROWSER_FRAGMENT);
        super.onDestroyView();
    }
}
