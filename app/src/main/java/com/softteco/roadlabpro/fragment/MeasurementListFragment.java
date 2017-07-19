package com.softteco.roadlabpro.fragment;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.Toast;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.adapters.MeasurementAdapter;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.MeasurementDAO;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.MeasurementModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.ui.SelectProjectDialog;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.view.SwipeMenuListView;

import java.util.Date;

/**
 * Created by bogdan on 08.04.2016.
 */
public class MeasurementListFragment extends SwipeListFragment {

    private final String TAG = MeasurementListFragment.class.getName();
    public static final String ARG_MEASUREMENT_LIST = "ARG_MEASUREMENT_LIST";

    private MeasurementDAO measurementDAO;
    private RoadModel roadModel;
    private Cursor dataCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkArgs();
        measurementDAO = new MeasurementDAO(getActivity());
        dataCursor = measurementDAO.getAllMeasurementByRoadIdCursor(roadModel.getId());
    }

    private void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(ARG_MEASUREMENT_LIST) != null) {
            roadModel = (RoadModel) getArguments().getSerializable(ARG_MEASUREMENT_LIST);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshItems();
    }

    @Override
    protected void updateTitle() {
        setTitle(roadModel.getName());
    }

    @Override
    protected void init() {
        super.init();
//        if (measurementDAO.getAllRoadsByFolderId(roadModel.getId()).size() < 1) {
//            RoadModel roadModel = new RoadModel(getActivity().getString(R.string.default_road_name), this.roadModel.getId());
//            measurementDAO.putRoad(roadModel);
//        }
    }

    @Override
    protected void initUI(final View view) {
        if (getMainActivity() != null && getMainActivity().getAddButton() != null) {
            getMainActivity().getAddButton().setVisibility(View.VISIBLE);
        }
        listView = (SwipeMenuListView) view.findViewById(R.id.fr_measurement_list);
        listView.setSelector(R.drawable.black_selectable_bgr);
        listView.setDrawSelectorOnTop(true);
        listView.setBackgroundColor(getResources().getColor(R.color.listview_bg));
    }

    @Override
    protected void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor c = (Cursor) getAdapter().getItem(position);
        MeasurementModel measurementModel = measurementDAO.cursorToMeasurement(c);
        open(measurementModel);
    }

    @Override
    protected CursorAdapter createAdapter() {
        return new MeasurementAdapter(getActivity(), dataCursor, 0, measurementDAO, roadModel.getId());
    }

    public void refreshItems() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void[] params) {
                MeasurementsDataHelper.getInstance().refreshMeasurementsCountersSync();
                final Cursor cursor = measurementDAO.getAllMeasurementByRoadIdCursor(roadModel.getId());
                return cursor;
            }
            @Override
            protected void onPostExecute(Cursor o) {
                super.onPostExecute(o);
                ((CursorAdapter)getAdapter()).swapCursor(o);
            }
        }.execute();
    }

    @Override
    protected BaseModel getModelItem(int position) {
        final Cursor c = (Cursor) getAdapter().getItem(position);
        MeasurementModel processedDataModel = measurementDAO.cursorToMeasurement(c);
        return processedDataModel;
    }

    @Override
    protected int getMoreDlgType() {
        return 2;
    }

    @Override
    public String getDeleteItemMsgId(BaseModel model) {
        String msgStr = "";
        if (model instanceof MeasurementModel) {
            Date measurementDate = new Date(((MeasurementModel)model).getDate());
            String measurementStr = DateUtil.format(measurementDate, DateUtil.Format.DDMMYYYHHMM);
            msgStr = getAppResouces().getString(R.string.delete_measurement_str, measurementStr);
        }
        return msgStr;
    }

    @Override
    protected void delete(final BaseModel model) {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void[] params) {
                MeasurementModel measurement = (MeasurementModel) model;
                MeasurementsDataHelper.getInstance().deleteMeasurement(measurement.getId(), true);
                final Cursor cursor = measurementDAO.getAllMeasurementByRoadIdCursor(roadModel.getId());
                return cursor;
            }
            @Override
            protected void onPostExecute(Cursor o) {
                super.onPostExecute(o);
                ((CursorAdapter)getAdapter()).swapCursor(o);
            }
        }.execute();
    }

    @Override
    protected void open(BaseModel model) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(MeasurementDetailsFragment.ARG_MEASUREMENT_MODEL, model);
        replaceFragment(MeasurementDetailsFragment.newInstance(bundle), true);
    }

    @Override
    protected void move(BaseModel model) {
        final MeasurementModel measurement = (MeasurementModel) model;
        SelectProjectDialog dialog = new SelectProjectDialog(getMainActivity(),
            new SelectProjectDialog.ProjectSelectDialogListener() {
                @Override
                public void onProjectSelected(long folderId, long roadId) {
                    moveMeasurementTo(measurement.getId(), roadId, folderId);
                }
            });
        dialog.setRoad(measurement.getRoadId());
        dialog.setCurrentPage(0);
        dialog.showRoadsScreen(true);
        dialog.show();
    }

    private void moveMeasurementTo(long measurementId, long roadId, long folderId) {
        MeasurementsDataHelper.getInstance().moveMeasurementTo(measurementId, folderId, roadId,
            new MeasurementsDataHelper.MeasurementsDataLoaderListener() {
                @Override
                public void onDataLoaded(Object data) {
                    refreshItems();
                    Toast.makeText(getContext(), getString(R.string.measurement_moved_text), Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    protected Cursor getSearchCursor(CharSequence searchStr) {
        return null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
        return ScreenItems.SCREEN_MEASUREMENT;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }

}
