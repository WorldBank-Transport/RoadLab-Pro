package com.softteco.roadlabpro.fragment;


import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.sensors.GPSDetector;
import com.softteco.roadlabpro.sensors.IntervalsRecordHelper;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.util.PreferencesUtil;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class AbstractWBFragmentWithProjectTitle extends AbstractWBFragment {

    protected CharSequence currentTitle = "";

    public void runUpdateTitleTask() {
        new AsyncTask<Object, Object, CharSequence>() {
            @Override
            protected CharSequence doInBackground(Object... params) {
                long curProject = RAApplication.getInstance().getCurrentFolderId();
                long curRoad = RAApplication.getInstance().getCurrentRoadId();
                FolderModel project = MeasurementsDataHelper.getInstance().getProject(curProject);
                RoadModel road = MeasurementsDataHelper.getInstance().getRoad(curProject, curRoad);
                CharSequence title = updateTitleAction(project, road);
                return title;
            }
            @Override
            protected void onPostExecute(CharSequence title) {
                if (!TextUtils.isEmpty(title)) {
                    currentTitle = title;
                    updateActionBarTitle(title);
                }
            }
        }.execute();
    }

    protected CharSequence updateTitleAction(FolderModel project, RoadModel road) {
        CharSequence title = null;
        if (project != null && road != null) {
            title = String.format("%s - %s", project.getName(), road.getName());
        }
        return title;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        runUpdateTitleTask();
    }

    @Override
    public void refresh() {
        runUpdateTitleTask();
    }
}
