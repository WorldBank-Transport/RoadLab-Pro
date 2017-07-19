package com.softteco.roadlabpro.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.rest.dto.AccountData;
import com.softteco.roadlabpro.rest.dto.GoogleToken;
import com.softteco.roadlabpro.sync.google.GoogleAPIHelper;
import com.softteco.roadlabpro.sync.SyncDataManager;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class LoginGoogleDialog extends Dialog {

    private static final String TAG = LoginGoogleDialog.class.getSimpleName();

    private WebView web;
    private ProgressDialog pDialog;
    private SyncDataManager.OnSyncDataListener callback;
    private GoogleAPIHelper apiHelper;

    public LoginGoogleDialog(Context context, GoogleAPIHelper helper, SyncDataManager.OnSyncDataListener callback) {
        super(context);
        this.callback = callback;
        this.apiHelper = helper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_auth_google);
        web = (WebView) findViewById(R.id.webv);
        web.resumeTimers();
        web.stopLoading();
        web.loadUrl("about:blank");
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAppCacheEnabled(true);
        web.getSettings().setAllowFileAccess(true);
        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE/*LOAD_DEFAULT*/);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        web.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        web.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        web.setVerticalScrollBarEnabled(false);
        web.setHorizontalScrollBarEnabled(false);
        //web.setVisibility(View.GONE);
        String url = GoogleAPIHelper.getUrl();
        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put(Constants.USER_AGENT_HEADER, Constants.USER_AGENT_HEADER_VALUE);
        web.loadUrl(url, extraHeaders);
        web.setWebViewClient(new WebViewClient() {
            boolean authComplete = false;
            String authCode;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                web.setVisibility(View.VISIBLE);
                showProgress(false);
                if (url.contains(GoogleAPIHelper.GOOGLE_CODE_PARAM) && authComplete != true) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i(TAG, "CODE: " + authCode);
                    PreferencesUtil.getInstance().setStringValue(GoogleAPIHelper.GOOGLE_CODE_PARAM, authCode);
                    authComplete = true;
                    obtainNewToken(authCode);
                    //Toast.makeText(context, "Authorization Code is: " + authCode, Toast.LENGTH_SHORT).show();
                } else if (url.contains(GoogleAPIHelper.GOOGLE_ERROR_ACCESS_DENIED_PARAM)) {
                    Log.i(TAG, "ACCESS_DENIED_HERE");
                    authComplete = true;
                    //Toast.makeText(context, "Error Occured", Toast.LENGTH_SHORT).show();
                    if (callback != null) {
                        callback.onComplete(false);
                    }
                    dismiss();
                }
            }
        });
    }

    public GoogleAPIHelper getApiHelper() {
        if (apiHelper == null) {
            apiHelper = new GoogleAPIHelper(getContext());
        }
        return apiHelper;
    }

    private void obtainNewToken(String authCode) {
        showProgress(true);
        getApiHelper().obtainNewToken(authCode, new Callback<GoogleToken>() {
            @Override
            public void onResponse(Response<GoogleToken> response, Retrofit retrofit) {
                GoogleToken googleToken = null;
                if (response.body() != null) {
                    googleToken = response.body();
                }
                String token = "";
                if (googleToken != null) {
                    token = googleToken.getAccessToken();
                }
                if (TextUtils.isEmpty(token)) {
                    Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                } else {
                    getApiHelper().rememberToken(googleToken);
                }
                checkUserName();
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "obtainNewToken", t);
                closeDialog(false);
            }
        });
    }

    private void closeDialog(boolean success) {
        if (callback != null) {
            callback.onComplete(success);
        }
        showProgress(false);
        dismiss();
    }

    private void checkUserName(Callback<AccountData> callback) {
        getApiHelper().getAccountData(callback);
    }

    private void checkUserName() {
        checkUserName(new Callback<AccountData>() {
            @Override
            public void onResponse(Response<AccountData> response, Retrofit retrofit) {
                AccountData data = null;
                if (response != null) {
                    data = response.body();
                }
                String userName = "";
                if (data != null) {
                    if (data.getUser() != null && !TextUtils.isEmpty(data.getUser().getDisplayName())) {
                        userName = data.getUser().getDisplayName();
                    } else if (!TextUtils.isEmpty(data.getName())) {
                        userName = data.getName();
                    }
                }
                if (!TextUtils.isEmpty(userName)) {
                    PreferencesUtil.getInstance().setGoogleAccountUserName(userName);
                }
                closeDialog(true);
            }
            @Override
            public void onFailure(Throwable t) {
                closeDialog(false);
                Log.e(TAG, "checkUserName", t);
            }
        });
    }



    void showProgress(boolean show) {
        dismissProgressDialog();
        if (show) {
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage(getContext().getString(R.string.progressLoginGoogle));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
    }

    private void dismissProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        dismissProgressDialog();
        callback = null;
    }
}