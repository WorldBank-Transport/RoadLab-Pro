package com.softteco.roadlabpro;

import android.app.Application;
import android.app.Instrumentation;
import android.test.ApplicationTestCase;

import com.softteco.roadlabpro.util.KMLHelper;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class KMLGenerationTest extends ApplicationTestCase<RAApplication> {

    RAApplication mApplication;

    public KMLGenerationTest() {
        super(RAApplication.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        mApplication = getApplication();
    }

    @Override
    protected void runTest() throws Throwable {
        generateKML();
    }

    private void generateKML() {
        KMLHelper helper = new KMLHelper(getContext());
        helper.exportDataSync();
    }

}