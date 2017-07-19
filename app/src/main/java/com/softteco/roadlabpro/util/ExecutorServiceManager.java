package com.softteco.roadlabpro.util;

import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceManager {

    private static String TAG = ExecutorServiceManager.class.getSimpleName();

    private static final int AWAIT_TERMINATION_TIME = 10;
    private ExecutorService executorService;

    private boolean isExecutorActive() {
        return executorService != null && !executorService.isShutdown() && !executorService.isTerminated();
    }

    public void runOperation(final Runnable run) {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }

        if (isExecutorActive()) {
            executorService.execute(run);
        }
    }

    public void shutdown() {
        if (!isExecutorActive()) {
            return;
        }
        
        executorService.shutdown();
        try {
            executorService.awaitTermination(AWAIT_TERMINATION_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
