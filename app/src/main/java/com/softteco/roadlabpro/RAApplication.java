package com.softteco.roadlabpro;

import android.app.Application;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.softteco.roadlabpro.algorithm.IRITable;
import com.softteco.roadlabpro.crashsender.HockeySender;
import com.softteco.roadlabpro.sensors.GPSDetector;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.users.User;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import java.io.File;

@ReportsCrashes(formKey = "816c6ddb056948adb330321fe484114f",
        logcatArguments = {"-t", "100", "-v", "time"})
public class RAApplication extends Application {

    private static RAApplication instance;

    private volatile GPSDetector gpsDetector;
    private EventApp eventApp;
    private IRITable iriTable;
    private MeasurementsDataHelper measurementsDataHelper;
    private User user;

    private long currentRoadId = 0;
    private long currentFolderId = 0;
    private long currentMeasurementId = 0;
    private boolean projectsExists;
    private boolean recordStarted = false;

    @Override
    public void onCreate() {
        ACRA.init(this);
        HockeySender crashSender = new HockeySender();
        ACRA.getErrorReporter().setReportSender(crashSender);
        super.onCreate();
        instance = this;
        gpsDetector = new GPSDetector();
        eventApp = new EventApp();
        iriTable = new IRITable();
        measurementsDataHelper = new MeasurementsDataHelper();
        user = new User();
        iriTable.init();
        user.restore(this);
        initImageLoader();
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
        .threadPoolSize(5)
        .threadPriority(Thread.NORM_PRIORITY - 2)
        .denyCacheImageMultipleSizesInMemory()
        .memoryCache(new LruMemoryCache(2 * 1024))
        .memoryCacheSize(2 * 1024)
        .defaultDisplayImageOptions(new DisplayImageOptions.Builder()
        .showImageForEmptyUri(R.drawable.no_photo_group)
        .showImageOnFail(R.drawable.no_photo_group)
        .resetViewBeforeLoading(true)
        .displayer(new FadeInBitmapDisplayer(300)).build())
        .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
        com.nostra13.universalimageloader.utils.L.writeLogs(false);
    }

    public boolean isProjectsExists() {
        return projectsExists;
    }

    public void setProjectsExists(boolean projectsExists) {
        this.projectsExists = projectsExists;
    }

    public boolean isRecordStarted() {
        return recordStarted;
    }

    public void setRecordStarted(boolean recordStarted) {
        this.recordStarted = recordStarted;
    }

    public IRITable getIriTable() {
        return iriTable;
    }

    public GPSDetector getGpsDetector() {
        return gpsDetector;
    }

    public MeasurementsDataHelper getMeasurementsDataHelper() {
        return measurementsDataHelper;
    }

    /**
     * The method creates a new instance.
     *
     * @return instance RAApplication
     */
    public static RAApplication getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Application isn't initialized yet!");
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public EventApp getEventApp() {
        return eventApp;
    }

    public long getCurrentRoadId() {
        return currentRoadId;
    }

    public void setCurrentRoadId(long currentRoadId) {
        this.currentRoadId = currentRoadId;
    }

    public long getCurrentFolderId() {
        return currentFolderId;
    }

    public void setCurrentFolderId(long currentFolderId) {
        this.currentFolderId = currentFolderId;
    }

    public long getCurrentMeasurementId() {
        return currentMeasurementId;
    }

    public void setCurrentMeasurementId(long currentMeasurementId) {
        this.currentMeasurementId = currentMeasurementId;
    }
}
