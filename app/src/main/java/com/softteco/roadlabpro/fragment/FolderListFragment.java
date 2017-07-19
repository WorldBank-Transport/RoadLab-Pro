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
import com.softteco.roadlabpro.adapters.FoldersAdapter;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.dao.FolderDAO;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.sync.SyncDataType;
import com.softteco.roadlabpro.sync.SyncDataManager;
import com.softteco.roadlabpro.tasks.ExportMeasurementDB;
import com.softteco.roadlabpro.ui.CustomInputDialog;
import com.softteco.roadlabpro.ui.DropboxSyncDialog;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.ExportToCSVResult;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.TimeUtil;
import com.softteco.roadlabpro.view.SwipeMenuListView;


/**
 * MyIssueListFragment is an extends of {@link android.support.v4.app.ListFragment}.
 */
public class FolderListFragment extends SwipeListFragment {

    private FolderDAO folderDAO;
    private final String TAG = FolderListFragment.class.getName();

    public static FolderListFragment newInstance() {
        FolderListFragment fragment = new FolderListFragment();
        return fragment;
    }

    public FolderListFragment() {
    }

    @Override
    protected void init() {
        super.init();
        folderDAO = new FolderDAO(getActivity());
//        if (MeasurementsDataHelper.getInstance().getFoldersCount() < 1) {
//            MeasurementsDataHelper.getInstance().createDefaultProject();
//        }
    }

    @Override
    protected void initUI(final View view) {
        searchView = (EditText) view.findViewById(R.id.fr_folder_list_search);
        listView = (SwipeMenuListView) view.findViewById(R.id.fr_folder_list);
        listView.setSelector(R.drawable.black_selectable_bgr);
        listView.setDrawSelectorOnTop(true);
        listView.setBackgroundColor(getResources().getColor(R.color.listview_bg));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshFolders();
    }

    @Override
    protected void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Cursor c = (Cursor) parent.getAdapter().getItem(position);
        FolderModel folderModel = folderDAO.cursorToFolder(c);
        open(folderModel);
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_folder_list;
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_PROJECTS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return true;
    }

    protected BaseModel getModelItem(int position) {
        final Cursor c = (Cursor) getAdapter().getItem(position);
        FolderModel folder = folderDAO.cursorToFolder(c);
        return folder;
    }

    @Override
    public String getDeleteItemMsgId(BaseModel model) {
        String msgStr = "";
        if (model instanceof FolderModel) {
            msgStr = getAppResouces().getString(R.string.delete_folder_str, ((FolderModel)model).getName());
        }
        return msgStr;
    }

    @Override
    protected void showDeleteWarningDlg(final BaseModel model) {
        final FolderModel project = (FolderModel) model;
        if (project.isDefaultProject()) {
            showDefaultProjectToast();
            return;
        }
        super.showDeleteWarningDlg(model);
    }

    @Override
    protected void delete(final BaseModel model) {
        final FolderModel project = (FolderModel) model;
        if (project.isDefaultProject()) {
            showDefaultProjectToast();
            return;
        }
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void[] params) {
                MeasurementsDataHelper.getInstance().deleteProject(project.getId(), true);
                final Cursor cursor = folderDAO.getAllFolderCursor();
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
        bundle.putSerializable(RoadFragment.ARG_ROADS_MODEL, model);
        replaceFragment(RoadFragment.newInstance(bundle), true);
    }

    @Override
    protected void select(BaseModel model) {
//        final FolderModel folder = ((FolderModel) model);
//        new AsyncTask<Object, Object, Object>() {
//            @Override
//            protected Object doInBackground(Object... params) {
//                long folderId = folder.getId();
//                RAApplication.getInstance().setCurrentFolderId(folderId);
//                PreferencesUtil.getInstance().setCurrentFolderId(folderId);
//                RoadModel road = MeasurementsDataHelper.getInstance().getLastRoad(folderId);
//                if (road != null) {
//                    RAApplication.getInstance().setCurrentRoadId(road.getId());
//                    PreferencesUtil.getInstance().setCurrentRoadId(road.getId());
//                } else {
//                    RAApplication.getInstance().setCurrentRoadId(-1);
//                    PreferencesUtil.getInstance().setCurrentRoadId(-1);
//                }
//                final boolean isProjectsExists =
//                MeasurementsDataHelper.getInstance().isProjectsExists();
//                PreferencesUtil.getInstance().setProjectsExists(isProjectsExists);
//                RAApplication.getInstance().setProjectsExists(isProjectsExists);
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Object o) {
//                showToast(getAppResouces().getString(R.string.project_selected_text,
//                    folder.getName() == null ? "" : folder.getName()));
//            }
//        }.execute();
    }

    @Override
    protected void rename(BaseModel model) {
        final FolderModel project = (FolderModel) model;
        if (project.isDefaultProject()) {
            showDefaultProjectToast();
            return;
        }
        new CustomInputDialog(getContext(), getString(R.string.rename_folder), project.getName(),
            new CustomInputDialog.CustomInputDialogListener() {
            @Override
            public void onRequestClose(String value) {
                FolderModel newProject = project.clone();
                processRename(newProject, value);
            }
        }).show();
    }

    private void processRename(FolderModel project, String value) {
        project.setName(value);
        MeasurementsDataHelper.getInstance().updateProject(project,
            new MeasurementsDataHelper.MeasurementsDataLoaderListener() {
            @Override
            public void onDataLoaded(Object data) {
                refreshFolders();
            }
        });
    }

    @Override
    protected void sync(final BaseModel model) {
        final FolderModel folder = (FolderModel) model;
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
                            exportData(folder, value);
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

    private void exportData(final FolderModel model, final String dropboxPath) {
        new ExportMeasurementDB(getActivity(), new ExportToCSVResult() {
            @Override
            public void onResultsAfterExporting(String[] executed) {
                uploadDataToDropbox(model, dropboxPath);
            }
        }).execute();
    }

    protected void uploadDataToDropbox(final FolderModel model, final String dropboxPath) {
            PreferencesUtil.getInstance().incExportId();
            getDataSyncManager().uploadSingleProject(model, dropboxPath,
            new MeasurementsDataHelper.MeasurementsDataLoaderListener() {
                @Override
                public void onDataLoaded(Object data) {
                    showProgress(false);
                    refreshFolders();
                }
            });
    }

    @Override
    protected CursorAdapter createAdapter() {
        FoldersAdapter adapter = new FoldersAdapter(getActivity(), null, 0, folderDAO);
        return adapter;
    }

    @Override
    public void refresh() {
        refreshFolders();
    }

    public void refreshFolders() {
        new AsyncTask<Void, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Void[] params) {
                //long folderId = folderDAO.putFolder(folderModel);
                MeasurementsDataHelper.getInstance().refreshFoldersCountersSync();
                final Cursor cursor = folderDAO.getAllFolderCursor();
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
    protected Cursor getSearchCursor(CharSequence searchStr) {
        if (!TextUtils.isEmpty(searchStr)) {
            return folderDAO.searchFoldersCursor(searchStr.toString());
        } else {
            return folderDAO.getAllFolderCursor();
        }
    }
}
