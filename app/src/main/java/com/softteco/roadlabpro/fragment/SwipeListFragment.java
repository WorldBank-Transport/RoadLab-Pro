package com.softteco.roadlabpro.fragment;

import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sqlite.model.BaseModel;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.sqlite.model.RoadModel;
import com.softteco.roadlabpro.tasks.ExportMeasurementDBIntoZIP;
import com.softteco.roadlabpro.ui.CommonDialogs;
import com.softteco.roadlabpro.ui.SettingsDialog;
import com.softteco.roadlabpro.ui.SwipeListItemOptionsDialog;
import com.softteco.roadlabpro.ui.SwipeListItemSelectionTypes;
import com.softteco.roadlabpro.util.EMailUtil;
import com.softteco.roadlabpro.util.ExportToCSVResult;
import com.softteco.roadlabpro.util.KeyboardUtils;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.UIUtils;
import com.softteco.roadlabpro.view.SwipeMenu;
import com.softteco.roadlabpro.view.SwipeMenuCreator;
import com.softteco.roadlabpro.view.SwipeMenuItem;
import com.softteco.roadlabpro.view.SwipeMenuListView;

/**
 * MyIssueListFragment is an extends of {@link android.support.v4.app.ListFragment}.
 */
public abstract class SwipeListFragment extends AbstractWBFragment {

    protected SwipeMenuListView listView;
    protected EditText searchView;
    private BaseAdapter adapter;

    private final String TAG = SwipeListFragment.class.getName();

    public SwipeListFragment() {
        /**/
    }

    @Override
    public void onActivityCreated(final @Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        initAdapter();
        initSwipeMenuList();
    }

    protected void init() {
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUI(view);
        readMainSearchText();
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onListItemClick(parent, view, position, id);
                }
            });
        }
        if (searchView != null) {
            searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    startSearch(s);
                }
            });
            searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        startSearch(getSearchText());
                        KeyboardUtils.hideKeyboard(getMainActivity());
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    protected void showDefaultProjectToast() {
        showToast(R.string.default_road_restricted_text);
    }

    protected void showToast(int msgId) {
        Toast.makeText(getContext(), getAppResouces().getText(msgId), Toast.LENGTH_LONG).show();
    }

    protected void showToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    protected void initUI(final View view) {
    }

    protected void onListItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    protected boolean isUserEmployee() {
        return PreferencesUtil.getInstance().hasEmployeeLogin();
    }

    protected void readMainSearchText() {
        String searchString = getMainActivity().getSearchString();
        if (searchView != null && getMainActivity() != null) {
            searchView.setText(searchString);
        }
    }

    protected void setMainSearchText(String text) {
        if (getMainActivity() != null) {
            getMainActivity().setSearchString(text);
        }
    }

    protected String getSearchText() {
        if (searchView != null && !TextUtils.isEmpty(searchView.getText())) {
            return searchView.getText().toString();
        }
        return "";
    }

    protected void startSearch(CharSequence search) {
        if (adapter != null && adapter instanceof Filterable && ((Filterable)adapter).getFilter() != null) {
            ((Filterable)adapter).getFilter().filter(search);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        String searchString = getSearchText();
        setMainSearchText(searchString);
        KeyboardUtils.hideKeyboard(getMainActivity());
    }

    protected void initAdapter() {
        initAdapter(false);
    }

    protected void initAdapter(boolean force) {
        if (force || adapter == null) {
            adapter = createAdapter();
        }
        if (adapter != null && adapter instanceof CursorAdapter) {
            ((CursorAdapter) adapter).setFilterQueryProvider(new MyFilterQueryProvider());
        }
        startSearch(getSearchText());
        listView.setAdapter(adapter);
    }

    public BaseAdapter getAdapter() {
        return adapter;
    }

    protected abstract BaseAdapter createAdapter();

    protected void initSwipeMenuList() {
        SwipeMenuCreator creator = getSwipeMenuCreator();
        listView.setMenuCreator(creator);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                BaseModel model = getModelItem(position);
                onSwipeMenuOptionSelected(index, model);
                return false;
            }
        });
        // set SwipeListener
        listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {
            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }
            @Override
            public void onSwipeEnd(int position) {
                // swipe end
            }
        });
    }

    protected String getAccountDataType() {
        String dataTypeName = PreferencesUtil.getInstance().getSyncProviderTypeName(getContext());
        return dataTypeName;
    }

    protected abstract BaseModel getModelItem(int position);

    private SwipeMenuCreator getSwipeMenuCreator() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                onCreateSwipeMenu(menu);
            }
        };
        return creator;
    }

    public void onCreateSwipeMenu(SwipeMenu menu) {
        SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
        deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
        deleteItem.setWidth(UIUtils.dp2px(getActivity(), 90));
        deleteItem.setIcon(R.drawable.ic_delete);
        menu.addMenuItem(deleteItem);

        SwipeMenuItem openItem = new SwipeMenuItem(getActivity());
        openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
        openItem.setWidth(UIUtils.dp2px(getActivity(), 90));
        openItem.setTitle(getActivity().getString(R.string.more));
        openItem.setTitleSize(18);
        openItem.setTitleColor(Color.WHITE);
        menu.addMenuItem(openItem);
    }

    public void onSwipeMenuOptionSelected(int index, BaseModel model) {
        switch (index) {
            case 0:
                showDeleteWarningDlg(model);
                break;
            case 1:
                more(model);
                break;
        }
    }

    protected void showDeleteWarningDlg(final BaseModel model) {
        String msgStr = getDeleteItemMsgId(model);
        CommonDialogs.buildAlertMessageDlg(getActivity(), msgStr, new CommonDialogs.OnOkListener() {
            @Override
            public void onDlgOkPressed() {
                delete(model);
            }
        }).show();
    }

    public String getDeleteItemMsgId(BaseModel model) {
        return "";
    }

    public void onMoreMenuItemSelected(BaseModel model, SwipeListItemSelectionTypes type) {
        switch(type) {
            case SELECT:
                select(model);
                break;
            case SEND:
                send(model);
                break;
            case SYNC:
                sync(model);
                break;
            case EDIT:
                edit(model);
                break;
            case RENAME:
                rename(model);
                break;
            case MOVE:
                move(model);
                break;
            case OPEN:
                open(model);
            case CANCEL:
                break;
        }
    }

    protected void send(final BaseModel model) {
        new ExportMeasurementDBIntoZIP(getContext(), model, new ExportToCSVResult() {
            @Override
            public void onResultsAfterExporting(String[] zip) {
                if (zip == null || zip.length == 0) {
                    return;
                }
                String name = "";
                if (model instanceof FolderModel) {
                    name = ((FolderModel) model).getName();
                } else if (model instanceof RoadModel) {
                    name = ((RoadModel) model).getName();
                }
                String subject = getAppResouces().getString(R.string.email_data_subject_text, name);
                EMailUtil.sendAttachFileMail(getContext(), zip, subject, EMailUtil.EMAIL_MIME_TYPE);
            }
        }).execute();
    }

    protected void sync(BaseModel model) {
    }

    protected void select(BaseModel model) {
    }

    protected void open(BaseModel model) {
    }

    protected void edit(BaseModel model) {
    }

    protected void move(BaseModel model) {
    }

    protected void rename(BaseModel model) {
    }

    protected void delete(BaseModel model) {
    }

    protected void more(final BaseModel model) {
        new SwipeListItemOptionsDialog(getContext(), getMoreDlgType(),
            new SettingsDialog.SettingsDialogListener() {
            @Override
            public void onRequestClose(int type) {
                if (type < SwipeListItemSelectionTypes.values().length) {
                    onMoreMenuItemSelected(model, SwipeListItemSelectionTypes.values()[type]);
                }
            }
        }).show();
    }

    protected int getMoreDlgType() {
        return 0;
    }

    protected abstract Cursor getSearchCursor(CharSequence searchStr);

    protected class MyFilterQueryProvider implements FilterQueryProvider {
        @Override
        public Cursor runQuery(CharSequence searchStr) {
            return getSearchCursor(searchStr);
        }
    }
}
