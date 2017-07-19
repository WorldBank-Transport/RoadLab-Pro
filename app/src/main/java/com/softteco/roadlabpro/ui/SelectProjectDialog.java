package com.softteco.roadlabpro.ui;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.adapters.FoldersAdapter;
import com.softteco.roadlabpro.adapters.RoadsAdapter;
import com.softteco.roadlabpro.fragment.AbstractWBFragment;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.FolderDAO;
import com.softteco.roadlabpro.sqlite.dao.RoadDAO;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;

public class SelectProjectDialog extends Dialog {

    private static final int MAX_PAGE = 2;

    private ListView valuesList;
    private ImageButton newProjectButton;
    private FolderDAO folderDao;
    private RoadDAO roadDao;

    protected ProjectSelectDialogListener dialogListener;

    private CursorAdapter adapter;
    private int currentPage;
    private long selectedFolder = -1;
    private long selectedRoad = -1;

    private boolean showRoadsScreen = true;

    public SelectProjectDialog(final Context context, final ProjectSelectDialogListener listener) {
        super(context);
        dialogListener = listener;
    }

    public int getContentId() {
        return R.layout.dialog_list_select_project;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setFolder(long folder) {
        this.selectedFolder = selectedFolder;
    }

    public void setRoad(long road) {
        this.selectedRoad = selectedRoad;
    }

    public void showRoadsScreen(boolean show) {
        this.showRoadsScreen = show;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getContentId());
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        folderDao = new FolderDAO(RAApplication.getInstance());
        roadDao = new RoadDAO(RAApplication.getInstance());
        initUI();
    }

    protected void initUI() {
        setDlgTitle(getContext().getString(R.string.select_project_title));
        newProjectButton = (ImageButton) findViewById(R.id.add_new_project);
        valuesList = (ListView) findViewById(R.id.valuesList);
        valuesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onItemSelected(position);
            }
        });
        newProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage == 0) {
                    showAddProjectDialog();
                } else {
                    showAddRoadDialog();
                }
            }
        });
        initList();
    }

    private void showAddProjectDialog() {
        String title = getContext().getString(R.string.add_new_folder);
        showAddDialog(title);
    }

    private void showAddRoadDialog() {
        getCurrentProject(selectedFolder);
    }

    private void showAddDialog(String title) {
        new CustomInputDialog(getContext(), title,
        new CustomInputDialog.CustomInputDialogListener() {
            @Override
            public void onRequestClose(String value) {
                if (currentPage == 0) {
                    addProject(value);
                } else {
                    addRoad(selectedFolder, value);
                }
            }
        }).show();
    }

    private void getCurrentProject(long projectId) {
        MeasurementsDataHelper.getInstance().getProjectAsync(projectId,
        new MeasurementsDataHelper.MeasurementsDataLoaderListener<FolderModel>() {
            @Override
            public void onDataLoaded(FolderModel data) {
                String projName = "";
                String dlgTitle = "";
                if (data != null) {
                    projName = data.getName();
                }
                if (TextUtils.isEmpty(projName)) {
                    dlgTitle = getContext().getString(R.string.add_new_road);
                } else {
                    dlgTitle = getContext().getString(R.string.add_new_road_title, projName);
                }
                showAddDialog(dlgTitle);
            }
        });
    }

    private void addProject(String name) {
        FolderModel folderModel = new FolderModel(name);
        MeasurementsDataHelper.getInstance().createNewProject(folderModel, false,
        new MeasurementsDataHelper.MeasurementsDataLoaderListener<Boolean>() {
            @Override
            public void onDataLoaded(Boolean data) {
                initList();
            }
        });
    }

    private void addRoad(long folderId, String name) {
        RoadModel roadModel = new RoadModel(name, folderId);
        MeasurementsDataHelper.getInstance().createNewRoad(roadModel, false,
        new MeasurementsDataHelper.MeasurementsDataLoaderListener<Boolean>() {
            @Override
            public void onDataLoaded(Boolean data) {
                initList();
            }
        });
    }

    private void onItemSelected(int position) {
        final Cursor c = (Cursor) adapter.getItem(position);
        if (currentPage == 0) {
            FolderModel folder = folderDao.cursorToFolder(c);
            selectedFolder = folder.getId();
            currentPage++;
            if (showRoadsScreen) {
                initList();
                return;
            }
        }
        if (currentPage == 1) {
            if (showRoadsScreen) {
                RoadModel road = roadDao.cursorToRoad(c);
                selectedRoad = road.getId();
            }
            currentPage++;
        }
        if (currentPage == MAX_PAGE) {
            if (dialogListener != null) {
                dialogListener.onProjectSelected(selectedFolder, selectedRoad);
            }
            dismiss();
        }
    }

    private void initList() {
        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                if (currentPage == 0) {
                    Cursor foldersCursor = folderDao.getAllFolderCursor();
                    adapter = new FoldersAdapter(getContext(), foldersCursor, 0, folderDao);
                } else if (currentPage == 1) {
                    Cursor roadsCursor = null;
                    if (selectedFolder < 0) {
                        roadsCursor = roadDao.getAllRoadCursor();
                    } else {
                        roadsCursor = roadDao.getAllRoadByFolderIdCursor(selectedFolder);
                    }
                    adapter = new RoadsAdapter(getContext(), roadsCursor, 0, roadDao, selectedFolder);
                }
                return null;
            }
            @Override
            protected void onPostExecute(Object o) {
                setupTitle();
                valuesList.setAdapter(adapter);
            }
        }.execute();
    }

    private void setupTitle() {
        if (currentPage == 0) {
            setDlgTitle(getContext().getString(R.string.select_project_title));
        } else if (currentPage == 1) {
            setDlgTitle(getContext().getString(R.string.select_road_title));
        }
    }

    public void setDlgTitle(String title) {
        TextView headerText = (TextView) findViewById(R.id.header_title);
        if (headerText != null) {
            headerText.setText(title);
        }
    }

    public interface ProjectSelectDialogListener {
        void onProjectSelected(long folderId, long roadId);
    }
}
