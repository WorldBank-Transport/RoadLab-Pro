package com.softteco.roadlabpro.ui;

import android.content.Context;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;
import com.softteco.roadlabpro.adapters.ExportListAdapter;
import com.softteco.roadlabpro.adapters.SuspensionListAdapter;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.Arrays;

public class ExportDialog extends SettingsListDialog {

    public ExportDialog(Context context, SettingsDialogListener listener) {
        super(context, listener);
    }

    @Override
    protected void initUI() {
        super.initUI();
        setDlgTitle(getContext().getString(R.string.export_title_dialog));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new ExportListAdapter(getContext(), Arrays.asList(PreferencesUtil.EXPORT_TYPES.values()));
    }
}
