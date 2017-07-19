package com.softteco.roadlabpro.ui;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;
import com.softteco.roadlabpro.adapters.MeasurementTypeListAdapter;
import com.softteco.roadlabpro.util.PreferencesUtil;

import java.util.Arrays;

public class MeasurementsFilterDialog extends SettingsListDialog implements View.OnClickListener {

    private TextView btnAccept;
    private TextView btnCancel;
    private MeasurementTypeListAdapter adapter;

    public MeasurementsFilterDialog(Context context, SettingsDialogListener listener) {
        super(context, listener);
    }

    @Override
    protected void initUI() {
        super.initUI();
        setDlgTitle(getContext().getString(R.string.select_filter));
        btnAccept = (TextView) findViewById(R.id.btnAccept);
        btnCancel = (TextView) findViewById(R.id.btnCancel);
        btnAccept.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.initSelection();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnAccept:
                if (adapter != null) {
                    adapter.saveSelection();
                }
                if (dialogListener != null) {
                    dialogListener.onRequestClose(0);
                }
            case R.id.btnCancel:
                dismiss();
                break;
        }
    }

    protected void onListItemClick(int position) {
        if (adapter != null) {
            adapter.turnSelection(position);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected BaseListAdapter getAdapter() {
        adapter = new MeasurementTypeListAdapter(getContext(),
        Arrays.asList(PreferencesUtil.MEASUREMENT_TYPES.values()));
        return adapter;
    }

    @Override
    public int getContentId() {
        return R.layout.measurements_list_dialog;
    }
}
