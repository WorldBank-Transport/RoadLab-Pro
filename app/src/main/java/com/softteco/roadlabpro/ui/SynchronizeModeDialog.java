package com.softteco.roadlabpro.ui;

import android.content.Context;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;
import com.softteco.roadlabpro.adapters.SyncModeListAdapter;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.Arrays;

public class SynchronizeModeDialog extends SettingsListDialog {

    public SynchronizeModeDialog(Context context, SettingsDialogListener listener) {
        super(context, listener);
    }

    @Override
    protected void initUI() {
        super.initUI();
        setDlgTitle(getContext().getString(R.string.synchronize));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new SyncModeListAdapter(getContext(), Arrays.asList(PreferencesUtil.SYNC_MODES.values()));
    }
}
