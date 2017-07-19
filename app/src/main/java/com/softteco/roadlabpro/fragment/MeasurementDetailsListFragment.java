package com.softteco.roadlabpro.fragment;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.BaseListAdapter;
import com.softteco.roadlabpro.adapters.MeasurementItemsListAdapter;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementItem;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.util.UIUtils;
import com.softteco.roadlabpro.view.SwipeMenu;
import com.softteco.roadlabpro.view.SwipeMenuItem;
import com.softteco.roadlabpro.view.SwipeMenuListView;

import java.util.List;

public class MeasurementDetailsListFragment extends SwipeListFragment {

    private final String TAG = MeasurementDetailsListFragment.class.getName();
    public static final String ARG_MEASUREMENT = "ARG_MEASUREMENT";

    private MeasurementModel measurementModel;

    private List<MeasurementItem> items;
    private BaseListAdapter adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkArgs();
    }

    @Override
    protected void updateTitle() {
        String title = FileUtils.getExportItemName(getContext(),
        measurementModel.getId(), measurementModel.getDate());
        setTitle(title);
    }

    private void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(ARG_MEASUREMENT) != null) {
            measurementModel = (MeasurementModel) getArguments().getSerializable(ARG_MEASUREMENT);
        }
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void initUI(final View view) {
        listView = (SwipeMenuListView) view.findViewById(R.id.fr_measurement_list);
        listView.setSelector(R.drawable.black_selectable_bgr);
        listView.setDrawSelectorOnTop(true);
        listView.setBackgroundColor(getResources().getColor(R.color.listview_bg));
        initAdapter();
    }

    @Override
    protected BaseAdapter createAdapter() {
        return new MeasurementItemsListAdapter(getActivity(), items, measurementModel);
    }

    @Override
    public void refresh() {
        initAdapter();
    }

    @Override
    public void onCreateSwipeMenu(SwipeMenu menu) {
        SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
        deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
        deleteItem.setWidth(UIUtils.dp2px(getActivity(), 90));
        deleteItem.setIcon(R.drawable.ic_delete);
        menu.addMenuItem(deleteItem);
    }

    @Override
    protected void initAdapter() {
        MeasurementsDataHelper.getInstance().
        getData(measurementModel, true, new MeasurementsDataHelper.MeasurementsDataLoaderListener<List<MeasurementItem>>() {
            @Override
            public void onDataLoaded(List<MeasurementItem> items) {
                MeasurementDetailsListFragment.this.items = items;
                if (adapter == null) {
                    adapter = (BaseListAdapter) createAdapter();
                    adapter.setSource(items);
                } else {
                    adapter.setSource(items, true);
                    adapter.notifyDataSetChanged();
                }
                if (listView != null && listView.getAdapter() == null) {
                    listView.setAdapter(adapter);
                }
            }
        });
    }

    @Override
    protected BaseModel getModelItem(int position) {
        return (BaseModel) items.get(position);
    }

    @Override
    public String getDeleteItemMsgId(BaseModel model) {
        String msgStr = getAppResouces().getString(R.string.delete_item_prompt_str);
        return msgStr;
    }

    @Override
    protected void delete(final BaseModel model) {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void[] params) {
                if (model instanceof MeasurementItem) {
                    MeasurementsDataHelper.getInstance().deleteItem((MeasurementItem) model);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Cursor o) {
                refresh();
            }
        }.execute();
    }

    @Override
    protected Cursor getSearchCursor(CharSequence searchStr) {
        return null;
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_measurement_list;
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_MEASUREMENT_DETAILS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }
}
