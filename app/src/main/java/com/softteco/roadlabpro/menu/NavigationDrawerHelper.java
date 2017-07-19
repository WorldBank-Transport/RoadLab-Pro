package com.softteco.roadlabpro.menu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.activity.BaseFragmentActivity;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.users.User;
import com.softteco.roadlabpro.util.ActivityUtil;
import com.softteco.roadlabpro.util.PreferencesUtil;

/**
 * Created by ppp on 07.04.2015.
 */
public class NavigationDrawerHelper {

    private NavigationDrawerFragment navigationDrawerFragment;

    public void onCreate(BaseFragmentActivity activity, Bundle bundle) {
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        navigationDrawerFragment = (NavigationDrawerFragment) activity.getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) activity.findViewById(R.id.drawer_layout));
    }


    public boolean isDrawerindIcatorEnabled() {
        if (navigationDrawerFragment != null && navigationDrawerFragment.getDrawerLayout() != null) {
            return navigationDrawerFragment.getDrawerToggle().isDrawerIndicatorEnabled();
        }
        return false;
    }

    public boolean isDrawerOpen() {
        if (navigationDrawerFragment != null) {
            return navigationDrawerFragment.isDrawerOpen();
        }
        return false;
    }

    public void setDrawerLockMode(int mode) {
        if (navigationDrawerFragment != null && navigationDrawerFragment.getDrawerLayout() != null) {
            navigationDrawerFragment.getDrawerLayout().setDrawerLockMode(mode);
        }
    }

    public void setHomeIndicator(boolean enabled) {
        if (navigationDrawerFragment != null) {
            navigationDrawerFragment.setHomeIndicator(enabled);
        }
    }

    public void openDrawer() {
        if (navigationDrawerFragment != null) {
            navigationDrawerFragment.openDrawer();
        }
    }

    public void closeDrawer() {
        if (navigationDrawerFragment != null) {
            navigationDrawerFragment.closeDrawer();
        }
    }

    public void toggleDrawer() {
        if (isDrawerOpen()) {
            closeDrawer();
        } else {
            openDrawer();
        }
    }

    public void setCurrentUser(User user) {
        if (navigationDrawerFragment != null) {
            navigationDrawerFragment.setCurrentUser(user);
        }
    }

    public void clearCurrentUserInfo() {
        if (navigationDrawerFragment != null) {
            navigationDrawerFragment.clearCurrentUserInfo();
        }
    }

    public void setCurrentProject(FolderModel folder) {
        if (navigationDrawerFragment != null) {
            navigationDrawerFragment.setCurrentProject(folder);
        }
    }

    public void setCurrentRoad(RoadModel road) {
        if (navigationDrawerFragment != null) {
            navigationDrawerFragment.setCurrentRoad(road);
        }
    }

    public void refreshNavigationMenu(boolean isProjectsExists) {
        if (navigationDrawerFragment != null) {
            navigationDrawerFragment.refreshNavigationMenu(isProjectsExists);
        }
    }

    public void refreshMenuHeader() {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                final boolean isProjectsExists = checkProjectsAvailability();
                FolderModel localFolder = null;
                RoadModel localRoad = null;
                if (isProjectsExists) {
                    long projectId = RAApplication.getInstance().getCurrentFolderId();
                    long roadId = RAApplication.getInstance().getCurrentRoadId();
                    double distance = MeasurementsDataHelper.getInstance().getOverallDistanceSync(projectId, -1, -1);
                    double pathDistance = MeasurementsDataHelper.getInstance().getPathDistanceSync(projectId, -1, -1);
                    localFolder = MeasurementsDataHelper.getInstance().getProject(projectId);
                    if (localFolder != null) {
                        localFolder.setOverallDistance(distance);
                        localFolder.setPathDistance(pathDistance);
                    }
                    localRoad = checkRoadSelection(projectId, roadId);
                }
                final FolderModel folder = localFolder;
                final RoadModel road = localRoad;
                ActivityUtil.runOnMainThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (isProjectsExists) {
                                    setCurrentProject(folder);
                                    setCurrentRoad(road);
                                }
                                refreshNavigationMenu(isProjectsExists);
                            }
                        });
                return null;
            }
        }.execute();
    }

    private boolean checkProjectsAvailability() {
        final boolean isProjectsExists =
        MeasurementsDataHelper.getInstance().isProjectsExists();
        PreferencesUtil.getInstance().setProjectsExists(isProjectsExists);
        RAApplication.getInstance().setProjectsExists(isProjectsExists);
        return isProjectsExists;
    }

    private RoadModel checkRoadSelection(long projectId, long roadId) {
        RoadModel road = MeasurementsDataHelper.getInstance().getRoad(projectId, roadId);
        if (road == null) {
            road = MeasurementsDataHelper.getInstance().getLastRoad(projectId);
            if (road != null) {
                PreferencesUtil.getInstance().setCurrentRoadId(road.getId());
                RAApplication.getInstance().setCurrentRoadId(road.getId());
            }
        }
        return road;
    }
}
