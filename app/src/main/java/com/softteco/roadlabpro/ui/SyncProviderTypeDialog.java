package com.softteco.roadlabpro.ui;

import android.content.Context;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;
import com.softteco.roadlabpro.adapters.SuspensionListAdapter;
import com.softteco.roadlabpro.adapters.SyncDataTypeListAdapter;
import com.softteco.roadlabpro.sync.SyncDataType;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.Arrays;

public class SyncProviderTypeDialog extends SettingsListDialog {

    public SyncProviderTypeDialog(Context context, SettingsDialogListener listener) {
        super(context, listener);
    }

    @Override
    protected void initUI() {
        super.initUI();
        setDlgTitle(getContext().getString(R.string.sync_data_type));
    }

    protected void onListItemClick(int position) {
        if (getAdapter() != null) {
            SyncDataType type = (SyncDataType) getAdapter().getItem(position);
            if (type != null && dialogListener != null) {
                dialogListener.onRequestClose(type.ordinal());
            }
        }
        dismiss();
    }

    @Override
    protected BaseListAdapter getAdapter() {
        return new SyncDataTypeListAdapter(getContext(), Arrays.asList(SyncDataType.values()));
    }
}
