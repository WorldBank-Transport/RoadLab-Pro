package com.softteco.roadlabpro.sync;

import android.content.Context;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sync.dropbox.DropboxManager;
import com.softteco.roadlabpro.sync.google.GoogleDriveManager;
import com.softteco.roadlabpro.util.PreferencesUtil;

public enum SyncDataType {

    DROPBOX (R.string.fr_settings_prividerType_dropbox),
    GOOGLE_DRIVE(R.string.fr_settings_prividerType_google);

    private int nameId;

    SyncDataType(int nameId) {
        this.nameId = nameId;
    }

    public static SyncDataManager getSyncDataManager(Context context) {
        SyncDataType type = PreferencesUtil.getInstance().getSyncProviderType();
        return getSyncDataManager(context, type);
    }

    public static SyncDataManager getSyncDataManager(Context context, SyncDataType type) {
        switch (type) {
            case DROPBOX:
                return new DropboxManager(context);
            case GOOGLE_DRIVE:
                return new GoogleDriveManager(context);
        }
        return null;
    }

    public int getNameId() {
        return nameId;
    }
}
