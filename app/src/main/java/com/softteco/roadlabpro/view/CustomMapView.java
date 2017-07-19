package com.softteco.roadlabpro.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

/**
 * Created by ppp on 14.05.2015.
 */
public class CustomMapView extends MapView {

    private boolean hasTextureViewSupport = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    private boolean isRGBA_8888ByDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

    private int detectedBestPixelFormat = -1;
    private boolean preventParentScrolling = true;
    private View drawingView;

    public CustomMapView(Context context) {
        super(context);
    }

    public CustomMapView(Context context, GoogleMapOptions options) {
        super(context, options);
    }

    public CustomMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void init() {
        View view = getRootView();
        if (view != null && view instanceof ViewGroup) {
            checkView((ViewGroup) view);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    private View searchAndFindDrawingView(ViewGroup group) {
        int childCount = group.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = group.getChildAt(i);
            if (child instanceof ViewGroup) {
                View view = searchAndFindDrawingView((ViewGroup) child);

                if (view != null) {
                    return view;
                }
            }

            if (child instanceof SurfaceView) {
                return child;
            }

            if (hasTextureViewSupport) { // if we have support for texture view
                if (child instanceof TextureView) {
                    return child;
                }
            }
        }
        return null;
    }

    private int detectBestPixelFormat() {

        //Skip check if this is a new device as it will be RGBA_8888 by default.
        if (isRGBA_8888ByDefault) {
            return PixelFormat.RGBA_8888;
        }

        Context context = getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        //Get display pixel format
        int displayFormat = display.getPixelFormat();

        if ( PixelFormat.formatHasAlpha(displayFormat) ) {
            return displayFormat;
        } else {
            return PixelFormat.RGB_565;//Fallback for those who don't support Alpha
        }
    }

    public void checkView(ViewGroup view) {

        //Transparent Color For Views, android.R.color.transparent dosn't work on all devices
        int transparent =  0x00000000;

        view.setBackgroundColor(transparent); // Set Root View to be
                                              // transparent to prevent black screen on load

        drawingView = searchAndFindDrawingView(view); // Find the view the map
        // is using for Open GL

        if (drawingView == null)
            return; // If we didn't get anything then abort

        drawingView.setBackgroundColor(transparent); // Stop black artifact from
                                                     // being left behind on scroll

        // Create On Touch Listener for MapView Parent Scrolling Fix - Many
        // thanks to Gemerson Ribas (gmribas) for help with this fix.
        OnTouchListener touchListener = new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {

                int action = event.getAction();

                switch (action) {

                    case MotionEvent.ACTION_DOWN:
                        // Disallow Parent to intercept touch events.
                        view.getParent().requestDisallowInterceptTouchEvent(preventParentScrolling);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow Parent to intercept touch events.
                        view.getParent().requestDisallowInterceptTouchEvent(!preventParentScrolling);
                        break;
                }

                // Handle View touch events.
                view.onTouchEvent(event);
                return false;
            }
        };

        // texture view
        if (hasTextureViewSupport) { // If we support texture view and the
            // drawing view is a TextureView then
            // tweak it and return the fragment view

            if (drawingView instanceof TextureView) {
                TextureView textureView = (TextureView) drawingView;
                // Stop Containing Views from moving when a user is interacting
                // with Map View Directly
                textureView.setOnTouchListener(touchListener);
                return;
            }
        }

        // Otherwise continue onto legacy surface view hack
        final SurfaceView surfaceView = (SurfaceView) drawingView;

        // Fix for reducing black view flash issues
        SurfaceHolder holder = surfaceView.getHolder();

        //Detect Display Format if we havn't already
        if (detectedBestPixelFormat == -1) {
            detectedBestPixelFormat = detectBestPixelFormat();
        }

        //Use detected best pixel format
        holder.setFormat(detectedBestPixelFormat);

        // Stop Containing Views from moving when a user is interacting with
        // Map View Directly
        surfaceView.setOnTouchListener(touchListener);
    }
}
