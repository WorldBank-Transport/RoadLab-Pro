package com.softteco.roadlabpro.ui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;

/**
 * Created by ppp on 09.06.2015.
 */
public class SettingsListDialog extends SettingsDialog {

    private ListView listView;

    public SettingsListDialog(Context context, SettingsDialogListener listener) {
        super(context, listener);
    }

    @Override
    protected void initUI() {
        listView = (ListView) findViewById(R.id.valuesList);
        BaseListAdapter adapter = getAdapter();
        if (adapter != null) {
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(position);
            }
        });
    }

    protected void onListItemClick(int position) {
        if (dialogListener != null) {
            dialogListener.onRequestClose(position);
        }
        dismiss();
    }

    protected BaseListAdapter getAdapter() {
        return null;
    }
}
