package com.softteco.roadlabpro.ui;


import android.content.Context;
import android.os.Bundle;

import com.softteco.roadlabpro.R;

public class DropboxSyncDialog extends CustomInputDialog {

    public DropboxSyncDialog(Context context, String titleDialog, CustomInputDialogListener listener) {
        super(context, titleDialog, listener);
    }

    public DropboxSyncDialog(final Context context, final String titleDialog,
        final String value, final CustomInputDialogListener photoListener) {
        super(context, titleDialog, value, photoListener);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_dropbox_enter_id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

}
