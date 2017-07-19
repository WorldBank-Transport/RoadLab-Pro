package com.softteco.roadlabpro.ui;

import android.content.Context;
import android.os.CountDownTimer;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;
import com.softteco.roadlabpro.adapters.SaveMeasurementListAdapter;

import java.util.Arrays;
import java.util.List;

public class SaveMeasurementToDialog extends SettingsListDialog {

    private static final long TIMER_LENGTH = 10000;

    private SaveMeasurementListAdapter adapter;
    private List<SaveMeasurementSelectionTypes> items;
    private CountDownTimer timer;
    private CharSequence defaultItemTitle = "";

    public SaveMeasurementToDialog(Context context, CharSequence defaultItemTitle, SettingsDialogListener listener) {
        super(context, listener);
        this.defaultItemTitle = defaultItemTitle;
    }

    private void initTimer() {
        timer = new CountDownTimer(TIMER_LENGTH, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                //int countdown = (int)(millisUntilFinished / 1000);
                //setDlgTitle(getContext().getString(R.string.save_to_title)
                //+ " (" + String.valueOf(countdown) +")");
                refreshUI(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                dismiss();
            }
        };
        timer.start();
    }

    private void refreshUI(long time) {
        if (adapter != null) {
            adapter.refreshDefaultItem(defaultItemTitle, time);
        }
    }

    @Override
    protected void initUI() {
        super.initUI();
        setDlgTitle(getContext().getString(R.string.save_to_title));
        initTimer();
    }

    @Override
    protected BaseListAdapter getAdapter() {
        items = getMenuItems();
        adapter = new SaveMeasurementListAdapter(getContext(), items);
        return adapter;
    }

    private List<SaveMeasurementSelectionTypes> getMenuItems() {
        return Arrays.asList(SaveMeasurementSelectionTypes.values());
    }

    @Override
    protected void onListItemClick(int position) {
        SaveMeasurementSelectionTypes type = items.get(position);
        if (dialogListener != null) {
            dialogListener.onRequestClose(type.ordinal());
        }
        dismiss();
    }
}
