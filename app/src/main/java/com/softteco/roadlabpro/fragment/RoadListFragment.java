package com.softteco.roadlabpro.fragment;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Toast;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.adapters.RoadsAdapter;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sync.SyncDataType;
import com.softteco.roadlabpro.sync.SyncDataManager;
import com.softteco.roadlabpro.tasks.ExportMeasurementDB;
import com.softteco.roadlabpro.ui.CustomInputDialog;
import com.softteco.roadlabpro.ui.DropboxSyncDialog;
import com.softteco.roadlabpro.ui.SelectProjectDialog;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.ExportToCSVResult;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.TimeUtil;
import com.softteco.roadlabpro.view.SwipeMenuListView;

/**
 * Created by bogdan on 06.04.2016.
 */
public class RoadListFragment extends SwipeListFragment {

    private final String TAG = RoadListFragment.class.getName();
    public static final String ARG_ROAD_LIST = "ARG_ROAD_LIST";

    private RoadDAO roadDAO;
    private FolderModel folder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkArgs();
        roadDAO = new RoadDAO(getActivity());
    }

    private void checkArgs() {
        if (getArguments() != null && getArguments().getSerializable(ARG_ROAD_LIST) != null) {
            folder = (FolderModel) getArguments().getSerializable(ARG_ROAD_LIST);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshRoads();
    }

    @Override
    protected void initUI(final View view) {
        if (getMainActivity() != null && getMainActivity().getAddButton() != null) {
            getMainActivity().getAddButton().setVisibility(View.VISIBLE);
        }
        searchView = (EditText) view.findViewById(R.id.fr_road_list_search);
        listView = (SwipeMenuListView) view.findViewById(R.id.fr_road_list);
        listView.setSelector(R.drawable.black_selectable_bgr);
        listView.setDrawSelectorOnTop(true);
        listView.setBackgroundColor(getResources().getColor(R.color.listview_bg));
    }

    @Override
    protected int getMoreDlgType() {
        return 1;
    }

    @Override
    protected void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor c = (Cursor) getAdapter().getItem(position);
        RoadModel road = roadDAO.cursorToRoad(c);
        open(road);
    }

    @Override
    protected void init() {
        super.init();
//        long folderId = folder.getId();
//        if (MeasurementsDataHelper.getInstance().getRoadsCount(folderId) < 1) {
//            MeasurementsDataHelper.getInstance().createDefaultRoad(folder.getId());
//        }
    }

    @Override
    protected CursorAdapter createAdapter() {
        return new RoadsAdapter(getActivity(), null, 0, roadDAO, folder.getId());
    }

    public void refreshRoads() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void[] params) {
                MeasurementsDataHelper.getInstance().refreshRoadsCountersSync();
                final Cursor cursor = roadDAO.getAllRoadByFolderIdCursor(folder.getId());
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
        RoadModel model = roadDAO.cursorToRoad(c);
        return model;
    }

    @Override
    protected void showDeleteWarningDlg(final BaseModel model) {
        final RoadModel road = (RoadModel) model;
        if (road.isDefaultRoad()) {
            showDefaultProjectToast();
            return;
        }
        super.showDeleteWarningDlg(model);
    }

    @Override
    public String getDeleteItemMsgId(BaseModel model) {
        String msgStr = "";
        if (model instanceof RoadModel) {
            msgStr = getAppResouces().getString(R.string.delete_road_str, ((RoadModel)model).getName());
        }
        return msgStr;
    }

    @Override
    protected void delete(final BaseModel model) {
        final RoadModel road = (RoadModel) model;
        if (road.isDefaultRoad()) {
            showDefaultProjectToast();
            return;
        }
        new AsyncTask<Void, Void, Cursor>() {

            @Override
            protected Cursor doInBackground(Void[] params) {
                MeasurementsDataHelper.getInstance().deleteRoad(road.getId(), true);
                final Cursor cursor = roadDAO.getAllRoadByFolderIdCursor(((RoadModel) model).getFolderId());
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
        bundle.putSerializable(MeasurementsFragment.ARG_MEASUREMENTS_MODEL, model);
        replaceFragment(MeasurementsFragment.newInstance(bundle), true);
    }

    @Override
    protected void select(BaseModel model) {
//        RoadModel road = ((RoadModel) model);
//        RAApplication.getInstance().setCurrentFolderId(road.getFolderId());
//        PreferencesUtil.getInstance().setCurrentFolderId(road.getFolderId());
//        RAApplication.getInstance().setCurrentRoadId(road.getId());
//        PreferencesUtil.getInstance().setCurrentRoadId(road.getId());
//        showToast(getAppResouces().getString(R.string.road_selected_text,
//            road.getName() == null ? "" : road.getName()));
    }

    @Override
    protected void rename(BaseModel model) {
        final RoadModel road = (RoadModel) model;
        if (road.isDefaultRoad()) {
            showDefaultProjectToast();
            return;
        }
        new CustomInputDialog(getContext(), getString(R.string.rename_road), road.getName(),
            new CustomInputDialog.CustomInputDialogListener() {
                @Override
                public void onRequestClose(String value) {
                    RoadModel newRoad = road.clone();
                    processRename(newRoad, value);
                }
        }).show();
    }

    private void processRename(RoadModel road, String value) {
        road.setName(value);
        MeasurementsDataHelper.getInstance().updateRoad(road,
            new MeasurementsDataHelper.MeasurementsDataLoaderListener() {
                @Override
                public void onDataLoaded(Object data) {
                    refreshRoads();
                    Toast.makeText(getContext(), getString(R.string.road_renamed_text), Toast.LENGTH_LONG).show();
                }
        });
    }

    @Override
    protected void sync(final BaseModel model) {
        final RoadModel road = (RoadModel) model;
        String dataTypeName = getAccountDataType();
        new DropboxSyncDialog(getContext(),
                getAppResouces().getString(R.string.dropbox_dialog_title, dataTypeName),
                    Constants.DROPBOX_DEFAULT_FOLDER + "/" +
                    TimeUtil.getFormattedDate(TimeUtil.DATE_DROPBOX_FOLDER_NAME_FORMAT),
                new CustomInputDialog.CustomInputDialogListener() {
                    @Override
                    public void onRequestClose(String value) {
                        showProgress(true);
                        if (getDataSyncManager().authentication()) {
                            if (getDataSyncManager().isFolderAlreadyExists(value)) {
                                Toast.makeText(
                                        getActivity(),
                                        getString(R.string.toast_error_folder_exist_in_dropbox_yet, value),
                                        Toast.LENGTH_LONG
                                ).show();
                                showProgress(false);
                                sync(model);
                            } else {
                                exportData(road, value);
                            }
                        } else {
                            showProgress(false);
                            Toast.makeText(getActivity(),
                                    getString(R.string.toast_login_to_dropbox_please),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }).show();
    }

    private void exportData(final RoadModel model, final String dropboxPath) {
        new ExportMeasurementDB(getActivity(), new ExportToCSVResult() {
            @Override
            public void onResultsAfterExporting(String[] executed) {
                uploadDataToDropbox(model, dropboxPath);
            }
        }).execute();
    }

    protected void uploadDataToDropbox(final RoadModel model, final String dropboxPath) {
            PreferencesUtil.getInstance().incExportId();
            getDataSyncManager().uploadSingleRoad(model, dropboxPath,
                    new MeasurementsDataHelper.MeasurementsDataLoaderListener() {
                        @Override
                        public void onDataLoaded(Object data) {
                            showProgress(false);
                            refreshRoads();
                        }
                    });
    }

    @Override
    protected void move(BaseModel model) {
        final RoadModel road = (RoadModel) model;
        if (road.isDefaultRoad()) {
            showDefaultProjectToast();
            return;
        }
        SelectProjectDialog dialog = new SelectProjectDialog(getMainActivity(),
        new SelectProjectDialog.ProjectSelectDialogListener() {
            @Override
            public void onProjectSelected(long folderId, long roadId) {
                moveRoadTo(road.getId(), folderId);
            }
        });
        dialog.setRoad(road.getId());
        dialog.setCurrentPage(0);
        dialog.showRoadsScreen(false);
        dialog.show();
    }

    private void moveRoadTo(long roadId, long folderId) {
        MeasurementsDataHelper.getInstance().moveRoadTo(folderId, roadId,
            new MeasurementsDataHelper.MeasurementsDataLoaderListener() {
                @Override
                public void onDataLoaded(Object data) {
                    refreshRoads();
                    Toast.makeText(getContext(), getString(R.string.road_moved_text), Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    protected Cursor getSearchCursor(CharSequence searchStr) {
        if (!TextUtils.isEmpty(searchStr)) {
            return roadDAO.searchRoadsByFolderIdCursor(searchStr.toString(), folder.getId());
        } else {
            return roadDAO.getAllRoadByFolderIdCursor(folder.getId());
        }
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_road_list;
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_ROADS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return false;
    }

    public FolderModel getFolder() {
        return folder;
    }

    public void setFolder(FolderModel folder) {
        this.folder = folder;
    }
}
