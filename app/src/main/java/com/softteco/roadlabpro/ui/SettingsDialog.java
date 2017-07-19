package com.softteco.roadlabpro.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.softteco.roadlabpro.R;

/**
 * Created by ppp on 09.06.2015.
 */
public class SettingsDialog extends Dialog {

    protected SettingsDialogListener dialogListener;
    public SettingsDialog(final Context context, final SettingsDialogListener listener) {
        super(context);
        dialogListener = listener;
    }

    public int getContentId() {
        return R.layout.dialog_list;
    }

    protected void initUI() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getContentId());
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        initUI();
    }

    public void setDlgTitle(String title) {
        TextView headerText = (TextView) findViewById(R.id.header_title);
        if (headerText != null) {
            headerText.setText(title);
        }
    }

    public interface SettingsDialogListener {
        void onRequestClose(int type);
    }
}
