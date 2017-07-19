package com.softteco.roadlabpro.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.fragment.AboutFragment;
import com.softteco.roadlabpro.fragment.AbstractFragment;
import com.softteco.roadlabpro.fragment.AbstractWBFragment;
import com.softteco.roadlabpro.fragment.FolderListFragment;
import com.softteco.roadlabpro.fragment.NewTagFragment;
import com.softteco.roadlabpro.fragment.TagsFragment;
import com.softteco.roadlabpro.menu.NavMenuItem;
import com.softteco.roadlabpro.menu.NavigationDrawerFragment;
import com.softteco.roadlabpro.fragment.RoadFragment;
import com.softteco.roadlabpro.fragment.SettingsFragment;
import com.softteco.roadlabpro.fragment.SummaryFragment;
import com.softteco.roadlabpro.menu.NavigationDrawerHelper;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sensors.GPSDetector;
import com.softteco.roadlabpro.sensors.GpsManager;
import com.softteco.roadlabpro.sensors.IntervalsCalculator;
import com.softteco.roadlabpro.sensors.IntervalsRecordHelper;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.ProcessedDataModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sync.SyncDataManager;
import com.softteco.roadlabpro.sync.SyncDataType;
import com.softteco.roadlabpro.ui.CommonDialogs;
import com.softteco.roadlabpro.ui.CustomInputDialog;
import com.softteco.roadlabpro.ui.SelectProjectDialog;
import com.softteco.roadlabpro.users.User;
import com.softteco.roadlabpro.util.ConnectivityUtils;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.util.KeyboardUtils;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.TimeUtil;

import java.io.File;
import java.io.IOException;

public class MainActivity extends BaseFragmentActivity implements
        IntervalsCalculator.OnIntervalCalculatedListener,
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        View.OnClickListener, ConnectivityUtils.ConnectivityInterface {

    private final String TAG = MainActivity.class.getName();

    private static final int TIME_CLOSING_SIDE_BAR = 275;

    private NavigationDrawerHelper drawerHelper;
    private IntervalsRecordHelper intervalsRecordHelper;
    private CharSequence title;
    private Button recordButton;
    private ImageButton addButton;
    private EditText editSearch;
    private GpsManager gpsManager = new GpsManager();
    private View container;
    private volatile boolean syncRunning;
    private ConnectivityUtils connectivityUtils;
    private SyncDataManager syncDataManager;

    private static final long TIME_SYNCHRONIZATION_FOR_ROAD_INTERVAL_AND_EVENT = 1000 * 120;
    private static final long TIME_SYNCHRONIZATION_MANUAL_MODE = 1000 * 60;
    private static final long MAX_INTERVAL_COUNT = 10;
    private volatile int intervalsCount = 0;
    private long lastTime = 0;
    private long forceSyncLastTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getGpsDetector() != null) {
            getGpsDetector().init();
        }
        setContentView(R.layout.activity_main);
        initUI();
        initSyncDataManager();
        drawerHelper = new NavigationDrawerHelper();
        drawerHelper.onCreate(this, savedInstanceState);
        title = getTitle();
        intervalsRecordHelper = new IntervalsRecordHelper();
        intervalsRecordHelper.init(this);
        intervalsRecordHelper.setOnIntervalCalculatedListener(this);
        if (!checkGPS(GpsManager.GPS_ENABLED_CODE_AUTO)) {
            initGpsDetector();
        }
        PreferencesUtil.getInstance(this).generateDataRecordSession();
        long currentFolderId = PreferencesUtil.getInstance(this).getCurrentFolderId();
        long currentRoadId = PreferencesUtil.getInstance(this).getCurrentRoadId();
        boolean isProjectsExists = PreferencesUtil.getInstance().isProjectsExists();
        RAApplication.getInstance().setProjectsExists(isProjectsExists);
        RAApplication.getInstance().setCurrentFolderId(currentFolderId);
        RAApplication.getInstance().setCurrentRoadId(currentRoadId);
        intervalsCount = 0;
        connectivityUtils = new ConnectivityUtils(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectivityUtils.getConnectReceiver(), intentFilter);
        checkUser();
        refreshMenuHeader();
        //exportOldDB();
    }

    public String checkUser() {
        String userName = getSyncDataManager().getUserName();
        if (userName.isEmpty()) {
            drawerHelper.clearCurrentUserInfo();
        } else {
            User user = new User();
            user.givenName = userName;
            drawerHelper.setCurrentUser(user);
        }
        return userName;
    }

    public void initSyncDataManager() {
        if (syncDataManager != null) {
            syncDataManager.deactivate();
        }
        syncDataManager = SyncDataType.getSyncDataManager(this);
        syncDataManager.onCreate();
    }

    public SyncDataManager getSyncDataManager() {
        return syncDataManager;
    }

    //    private void exportOldDB() {
//        new Thread( new Runnable() {
//            @Override
//            public void run() {
//                FileUtils.exportDB(MainActivity.this,
//                        DataBaseHelper.getInstance().getWritableDatabase().getPath(),
//                        FileUtils.getDataDir() + DataBaseHelper.getInstance().getDatabaseName());
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(MainActivity.this, "Export DB completed", Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//        }).start();
//    }

    private void initGpsDetector() {
        if (getGpsDetector() != null) {
            getGpsDetector().reset();
            getGpsDetector().setGpsIntervalsCheckListener(intervalsRecordHelper);
            getGpsDetector().start();
        }
    }

    public GPSDetector getGpsDetector() {
        if (RAApplication.getInstance() != null) {
            return RAApplication.getInstance().getGpsDetector();
        }
        return null;
    }

    public IntervalsRecordHelper getIntervalsRecordHelper() {
        return intervalsRecordHelper;
    }

    public void checkRoughnessDetectionEnabled() {
        if (getIntervalsRecordHelper() != null) {
            getIntervalsRecordHelper().reinit(this);
        }
    }

    public void setSleepMode(boolean enabled) {
        if (enabled) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void initUI() {
        container = findViewById(R.id.ac_main_container);
        recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setOnClickListener(this);
        addButton = (ImageButton) findViewById(R.id.ac_main_btn_add_new_issue);
        addButton.setOnClickListener(this);
        editSearch = (EditText) findViewById(R.id.ac_main_edit_search);
        boolean sleepModeEnabled = PreferencesUtil.getInstance(this).getSleepModeEnabled();
        setSleepMode(sleepModeEnabled);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    KeyboardUtils.hideKeyboard(MainActivity.this);
                    return true;
                }
                return true;
            }
        });
    }

    public void refreshMenuHeader() {
        if (drawerHelper != null) {
            drawerHelper.refreshMenuHeader();
        }
    }

    public void showRecordControls(boolean show) {
        if (recordButton != null) {
            recordButton.setVisibility(show == true ? View.VISIBLE : View.GONE);
        }
    }

    public boolean checkGPS() {
        return checkGPS(GpsManager.GPS_ENABLED_CODE);
    }

    public boolean checkGPS(int code) {
        if (gpsManager != null) {
            return gpsManager.dispatchProviders(this, code);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ac_main_btn_add_new_issue:
                final Fragment fragment = getCurrentFragment();
                if (fragment != null && fragment instanceof AbstractFragment) {
                    int type = ((AbstractFragment) fragment).getTypeFragment();
                    if (type == ScreenItems.SCREEN_PROJECTS
                       || type == ScreenItems.SCREEN_NEW_PROJECT) {
                        AbstractWBFragment callFragmment = null;
                        if (fragment instanceof AbstractWBFragment) {
                            callFragmment = (AbstractWBFragment) fragment;
                        }
                        openCreateNewFolderDialog(callFragmment);
                        break;
                    }
                    if (type == ScreenItems.SCREEN_ROADS) {
                        long folderId = ((RoadFragment)fragment).getFolder().getId();
                        openCreateNewRoadDialog((RoadFragment)fragment, folderId);
                        break;
                    }
                    if (type == ScreenItems.SCREEN_TAGS) {
                        openNewTagFragment(true);
                        break;
                    }
                }
                break;
        }
    }

    public void openNewTagFragment(final boolean addToBackStack) {
        boolean isRoadAndFolderExists = false;
        try {
            isRoadAndFolderExists = MeasurementsDataHelper.
                    getInstance().isRoadAndFolderExists();
        } catch (Exception e) {
            Log.d(TAG, "onClick", e);
        }
        if (isRoadAndFolderExists) {
            replaceFragment(NewTagFragment.newInstance(), addToBackStack);
        } else {
            Toast.makeText(this, getString(
                    R.string.toast_error_current_folder_and_road_not_selected),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void selectProjectDialog() {
        SelectProjectDialog dialog = new SelectProjectDialog(this,
                new SelectProjectDialog.ProjectSelectDialogListener() {
                    @Override
                    public void onProjectSelected(long folderId, long roadId) {
                        RAApplication.getInstance().setCurrentFolderId(folderId);
                        RAApplication.getInstance().setCurrentRoadId(roadId);
                        PreferencesUtil.getInstance().setCurrentFolderId(folderId);
                        PreferencesUtil.getInstance().setCurrentRoadId(roadId);
                        Fragment fragment = getCurrentFragment();
                        AbstractWBFragment callFragment = null;
                        if (fragment != null && fragment instanceof AbstractWBFragment) {
                            callFragment = (AbstractWBFragment) fragment;
                            callFragment.refresh();
                        }
                    }
                });
        dialog.setCurrentPage(0);
        dialog.showRoadsScreen(true);
        dialog.show();
    }

    public void openCreateNewRoadDialog(final AbstractWBFragment fragment, final long folderId) {
        MeasurementsDataHelper.getInstance().getProjectAsync(folderId,
        new MeasurementsDataHelper.MeasurementsDataLoaderListener<FolderModel>() {
            @Override
            public void onDataLoaded(FolderModel data) {
                String projName = "";
                if (data != null) {
                    projName = data.getName();
                }
                String dlgTitle = getString(R.string.add_new_road_title, projName);
                showCreateNewRoadDialog(fragment, dlgTitle, folderId);
            }
        });
    }

    public void showCreateNewRoadDialog(final AbstractWBFragment fragment, String title, final long folderId) {
        new CustomInputDialog(this, title, new CustomInputDialog.CustomInputDialogListener() {
            @Override
            public void onRequestClose(String value) {
                RoadModel roadModel = new RoadModel(value, folderId);
                MeasurementsDataHelper.getInstance().createNewRoad(roadModel,
                        new MeasurementsDataHelper.MeasurementsDataLoaderListener<Boolean>() {
                            @Override
                            public void onDataLoaded(Boolean data) {
                                if (fragment != null) {
                                    fragment.refresh();
                                }
                            }
                        });
            }
        }).show();
    }

    public void openCreateNewFolderDialog(final AbstractWBFragment fragment) {
        new CustomInputDialog(this, getString(R.string.add_new_folder), new CustomInputDialog.CustomInputDialogListener() {
            @Override
            public void onRequestClose(String value) {
                FolderModel folderModel = new FolderModel(value);
                MeasurementsDataHelper.getInstance().createNewProject(folderModel,
                new MeasurementsDataHelper.MeasurementsDataLoaderListener<Boolean>() {
                    @Override
                    public void onDataLoaded(Boolean data) {
                        if (fragment != null) {
                            fragment.refresh();
                        }
                    }
                });
            }
        }).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (syncDataManager != null) {
            syncDataManager.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (syncDataManager != null) {
            syncDataManager.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RAApplication.getInstance().setRecordStarted(false);
        if (syncDataManager != null) {
            syncDataManager.onDestroy();
        }
        if (intervalsRecordHelper != null) {
            intervalsRecordHelper.onDestroy();
        }
        if (connectivityUtils != null) {
            unregisterReceiver(connectivityUtils.getConnectReceiver());
        }
        if (getGpsDetector() != null) {
            getGpsDetector().destroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (syncDataManager != null) {
            syncDataManager.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == GpsManager.GPS_ENABLED_CODE_AUTO) {
            initGpsDetector();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerHelper != null) {
                    if (!drawerHelper.isDrawerindIcatorEnabled()) {
                        onBackPressed();
                    } else {
                        drawerHelper.toggleDrawer();
                    }
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(final NavMenuItem item) {
        KeyboardUtils.hideKeyboard(this);
        clearFragmentBackStack();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (item) {
                    case NEW_PROJECT:
                        Fragment fragment = getCurrentFragment();
                        AbstractWBFragment callFragment = null;
                        if (fragment != null && fragment instanceof AbstractWBFragment) {
                            callFragment = (AbstractWBFragment) fragment;
                        }
                        openCreateNewFolderDialog(callFragment);
                        break;
                    case NEW_ROAD:
                        fragment = getCurrentFragment();
                        callFragment = null;
                        if (fragment != null && fragment instanceof AbstractWBFragment) {
                            callFragment = (AbstractWBFragment) fragment;
                        }
                        final long folderId = RAApplication.getInstance().getCurrentFolderId();
                        openCreateNewRoadDialog(callFragment, folderId);
                        break;
                    case SELECT_PROJECT:
                        selectProjectDialog();
                        break;
                    case TAGS:
                        replaceFragment(TagsFragment.newInstance(null), false);
                        break;
                    case HEADER:
                    case QUICK_START:
                    case MEASUREMENT:
                        replaceFragment(SummaryFragment.newInstance(), false);
                        break;
                    case PROJECTS:
                        replaceFragment(FolderListFragment.newInstance(), false);
                        break;
                    case LOGIN:
                        break;
                    case SETTINGS:
                        replaceFragment(SettingsFragment.newInstance(), false);
                        break;
                    case ABOUT:
                        replaceFragment(AboutFragment.newInstance(), false);
                        break;
                    //case ScreenItems.SCREEN_REPORT_ISSUE:
                    //    replaceFragment(NewTagFragment.newInstance(), false);
                    //    break;
                }
            }
        }, TIME_CLOSING_SIDE_BAR);
    }

    private Fragment getCurrentFragment() {
        final Fragment fragment = getSupportFragmentManager().
        findFragmentById(R.id.ac_main_container);
        return fragment;
    }

    @Override
    public void onNavigationDrawerOpened() {
        refreshMenuHeader();
    }

    @Override
    public void onNavigationDrawerClosed() {
    }

    public void onSectionAttached(int number) {
        addButton.setVisibility(View.GONE);
        editSearch.setVisibility(View.GONE);
        switch (number) {
            case ScreenItems.SCREEN_MEASUREMENT:
                //title = getString(R.string.title_menu_measurement);
                container.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_TAGS:
                title = getString(R.string.title_menu_tags);
                container.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_PROJECTS:
                title = getString(R.string.title_menu_projects);
                container.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_NEW_TAG:
                title = getString(R.string.title_menu_tags);
                container.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_ROADS:
                container.setVisibility(View.VISIBLE);
                addButton.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_MEASUREMENTS:
                container.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_SETTINGS:
                title = getString(R.string.title_settings);
                container.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_ABOUT:
                title = getString(R.string.title_about);
                container.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_ISSUE_DETAILS:
                title = getString(R.string.title_menu_issue_details);
                container.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_TAG_DETAILS:
                title = getString(R.string.title_menu_tag_details);
                container.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_PROJECT_SUMMARY:
                container.setVisibility(View.VISIBLE);
                break;
            case ScreenItems.SCREEN_PDF_WEB_VIEW:
                title = getString(R.string.title_user_manual);
                container.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerHelper != null) {
            if (!drawerHelper.isDrawerindIcatorEnabled()){
                popBackStackToTop();
                return;
            } else if(drawerHelper.isDrawerOpen()) {
                drawerHelper.closeDrawer();
                return;
            }
        }
        if (RAApplication.getInstance().isRecordStarted()) {
            checkMeasurementSession();
        } else {
            goBack();
        }
    }

    private void goBack() {
        super.onBackPressed();
    }

    private void checkMeasurementSession() {
        String msgStr = getString(R.string.measurement_in_progress_warning_text);
        CommonDialogs.buildAlertMessageDlg(this, msgStr, new CommonDialogs.OnOkListener() {
            @Override
            public void onDlgOkPressed() {
                getIntervalsRecordHelper().stop();
                checkCurrentMeasurement();
                goBack();
            }
        }).show();
    }

    private void checkCurrentMeasurement() {
        MeasurementsDataHelper.getInstance().checkCurrentMeasurement(
        new MeasurementsDataHelper.MeasurementsDataLoaderListener<Boolean>() {
            @Override
            public void onDataLoaded(Boolean emptyMeasurement) {
            }
        });
    }

    public void restoreActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (drawerHelper != null && !drawerHelper.isDrawerOpen()) {
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    public void setHomeIndicator(boolean b) {
        if (drawerHelper != null) {
            drawerHelper.setHomeIndicator(b);
        }
    }

    public void setSearchString(String text) {
        if (editSearch != null) {
            editSearch.setText(text);
        }
    }

    public String getSearchString() {
        if (editSearch != null && editSearch.getText() != null) {
            return editSearch.getText().toString();
        }
        return "";
    }

    @Override
    public void popBackStackToTop() {
        Fragment fragment = super.popBackStackToTopWithFragment();
//        if (fragment != null && fragment instanceof AbstractFragment) {
//        }
        if (drawerHelper != null) {
            drawerHelper.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public void onIntervalCalculated(double distance, ProcessedDataModel data) {
        intervalsCount++;
        Log.d(TAG, "onIntervalCalculated: intervalsCount= " + intervalsCount);
        long currentTime = TimeUtil.getCurrentTimeMillis();
        if (currentTime - lastTime >= TIME_SYNCHRONIZATION_FOR_ROAD_INTERVAL_AND_EVENT) {
            lastTime = currentTime;
        }
    }

    private boolean isTimeToSynchronize() {
        if (intervalsCount >= MAX_INTERVAL_COUNT) {
            intervalsCount = 0;
            Log.d(TAG, "isTimeToSynchronize: true");
            return true;
        }
        return false;
    }

    public void runForceDataSync(boolean manualMode) {
        long curForceSyncTime = TimeUtil.getCurrentTimeMillis();
        if (manualMode || (curForceSyncTime - forceSyncLastTime >= TIME_SYNCHRONIZATION_MANUAL_MODE)) {
            Log.d(TAG, "runForceDataSync, manualMode: " + manualMode);
            forceSyncLastTime = curForceSyncTime;
        }
    }

//    public void runExportDataToCSV() {
//        for (int i = 0; i < 10; i++) {
//            File file = new File(FileUtils.getFileDir(this), "testFile_" + i + ".csv");
//            if (!file.exists()) {
//                try {
//                    file.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    @Override
    public void networkWasConnected() {
        Log.d(TAG, "networkWasConnected");
        intervalsCount = 0;
//        runForceDataSync(false);
//        uploadFileToDropBox("Belarus/Gomel-Minsk", "interval23.csv");
    }

    @Override
    public void setTitle(CharSequence title) {
        this.title = title;
    }

    public void updateActionBarTitle(CharSequence title) {
        setTitle(title);
        getSupportActionBar().setTitle(title);
    }

    public ImageButton getAddButton() {
        return addButton;
    }

}
