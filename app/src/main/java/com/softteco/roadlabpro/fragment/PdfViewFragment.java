package com.softteco.roadlabpro.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.joanzapata.pdfview.PDFView;
import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.menu.ScreenItems;

public class PdfViewFragment extends AbstractWBFragment {

    private static final String TAG = PdfViewFragment.class.getSimpleName();
    private static final String ASSET_NAME = "app_manual.pdf";
    private static final String PATH_TO_PDF = "file:///android_asset/" + ASSET_NAME;
    private static final String WEB_PATH_TO_PDF = "https://dl.dropboxusercontent.com/s/5ezpyc0jmmzp89e/app_manual.pdf";
    private static final String GOOGLE_DRIVE_LINK1 = "http://drive.google.com/viewerng/viewer?embedded=true&url=";
    private static final String GOOGLE_DRIVE_LINK2 = "https://docs.google.com/viewer?embedded=true&chrome=false&rm=demo&url=";

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WebView webView = (WebView) view.findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setBuiltInZoomControls(true);
        //webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(GOOGLE_DRIVE_LINK2 + WEB_PATH_TO_PDF);
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_pdf_web_view;
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_PDF_WEB_VIEW;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }
}
