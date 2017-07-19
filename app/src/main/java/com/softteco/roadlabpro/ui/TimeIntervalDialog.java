package com.softteco.roadlabpro.ui;

import android.content.Context;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;
import com.softteco.roadlabpro.adapters.TimeIntervalListAdapter;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.Arrays;

public class TimeIntervalDialog extends SettingsListDialog {

    public TimeIntervalDialog(Context context, SettingsDialogListener listener) {
        super(context, listener);
    }

    @Override
    protected void initUI() {
        super.initUI();
        setDlgTitle(getContext().getString(R.string.time_interval));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new TimeIntervalListAdapter(getContext(), Arrays.asList(PreferencesUtil.TIMEINTERVAL.values()));
    }
}