package com.softteco.roadlabpro.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.util.AppUtil;
import com.softteco.roadlabpro.util.PreferencesUtil;

/**
 * Created by Vadim Alenin on 3/24/2015.
 */
public class SplashActivity extends Activity {

    public static final int SPLASH_SCREEN_SHOW_DELAY_MILLIS = 2000;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final boolean isShowHelp = PreferencesUtil.getInstance(SplashActivity.this).isShowHelp();
                if (isShowHelp) {
                    AppUtil.showTutorial(SplashActivity.this);
                } else {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
                finish();
            }
        }, SPLASH_SCREEN_SHOW_DELAY_MILLIS);

    }
}
