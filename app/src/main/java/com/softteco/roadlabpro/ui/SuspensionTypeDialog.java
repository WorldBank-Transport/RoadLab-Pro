package com.softteco.roadlabpro.ui;

import android.content.Context;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;
import com.softteco.roadlabpro.adapters.SuspensionListAdapter;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.Arrays;

public class SuspensionTypeDialog extends SettingsListDialog {

    public SuspensionTypeDialog(Context context, SettingsDialogListener listener) {
        super(context, listener);
    }

    @Override
    protected void initUI() {
        super.initUI();
        setDlgTitle(getContext().getString(R.string.suspension_type));
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new SuspensionListAdapter(getContext(), Arrays.asList(PreferencesUtil.SUSPENSION_TYPES.values()));
    }
}
