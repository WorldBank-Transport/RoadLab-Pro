package com.softteco.roadlabpro.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;

import com.softteco.roadlabpro.sync.GetAccountCallback;
import com.softteco.roadlabpro.sync.SyncDataManager;
import com.softteco.roadlabpro.sync.SyncDataType;
import com.softteco.roadlabpro.tasks.ExportMeasurementDB;
import com.softteco.roadlabpro.ui.CommonDialogs;
import com.softteco.roadlabpro.ui.CustomInputDialog;
import com.softteco.roadlabpro.ui.DropboxSyncDialog;
import com.softteco.roadlabpro.ui.EnterIDDialog;
import com.softteco.roadlabpro.ui.ExportDialog;
import com.softteco.roadlabpro.ui.SettingsDialog;
import com.softteco.roadlabpro.ui.SuspensionTypeDialog;
import com.softteco.roadlabpro.ui.SyncProviderTypeDialog;
import com.softteco.roadlabpro.ui.TimeIntervalDialog;
import com.softteco.roadlabpro.users.User;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.DeviceUtil;
import com.softteco.roadlabpro.util.ExportToCSVResult;
import com.softteco.roadlabpro.util.FileUtils;
import com.softteco.roadlabpro.util.PreferencesUtil;
import com.softteco.roadlabpro.util.TimeUtil;
import com.softteco.roadlabpro.view.CustomButton;
import com.softteco.roadlabpro.view.TypefaceTextView;

/**
 * Created by elena on 4/25/15.
 */
public class SettingsFragment extends AbstractWBFragment {

    private final String TAG = SettingsFragment.class.getName();

    private boolean isLoginToDropBox;
    private boolean wasCancelled;
    private ExportMeasurementDB exportTask;
    private CustomButton dropBoxLoginButton;
    private CustomButton dropBoxLoadDataButton;
    private ProgressDialog progress;
    private AlertDialog alertToShow;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_settings;
    }

    @Override
    public int getMenuFragmentResources() {
        return -1;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_SETTINGS;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLoginToDropBox = getDataSyncManager().hasToken();
        Log.d(TAG, String.valueOf(isLoginToDropBox));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoginToDropBox && getDataSyncManager().authentication())
            dropboxLoadData();
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final PreferencesUtil.SUSPENSION_TYPES suspensionType = PreferencesUtil.getInstance(getMainActivity()).getSuspensionType();
        final float less50Thresholds = PreferencesUtil.getInstance(getMainActivity()).getLess50SpeedThresholds();
        final float more50Thresholds = PreferencesUtil.getInstance(getMainActivity()).getMore50SpeedThresholds();
        final PreferencesUtil.TIMEINTERVAL timeInterval = PreferencesUtil.getInstance(getMainActivity()).getTimeInterval();
        final SyncDataType syncDataType = PreferencesUtil.getInstance().getSyncProviderType();
        final String[] textHint = getResources().getStringArray(R.array.device_settings_common_hints);
        final String[] textValue = getResources().getStringArray(R.array.device_settings_common_values);

        final View vehicleView = view.findViewById(R.id.fr_settings_vehicle_block);
        final View androidView = view.findViewById(R.id.fr_settings_android_block);
        final View alwaysOnScreenView = view.findViewById(R.id.fr_settings_always_on_screen);
        final View timeIntervalView = view.findViewById(R.id.fr_settings_time_block);
        final View thresholdsLess50View = view.findViewById(R.id.fr_settings_thresholds_block);
        final View thresholdsMore50View = view.findViewById(R.id.fr_settings_more_thresholds_block);
        final View suspensionTypeView = view.findViewById(R.id.fr_settings_suspension_block);
        final View syncProviderTypeView = view.findViewById(R.id.fr_settings_sync_provider);

        final TypefaceTextView txtSyncProviderType = (TypefaceTextView) syncProviderTypeView.findViewById(R.id.settings_item_value);
        txtSyncProviderType.setText(getString(R.string.sync_data_type));
        final TypefaceTextView txtSyncProviderHint = (TypefaceTextView) syncProviderTypeView.findViewById(R.id.settings_item_hint);

        final TypefaceTextView txtSuspensionType = (TypefaceTextView) suspensionTypeView.findViewById(R.id.settings_item_value);
        txtSuspensionType.setText(getString(R.string.suspension_type));

        final TypefaceTextView txtSleepValue = (TypefaceTextView) alwaysOnScreenView.findViewById(R.id.settings_item_value);
        txtSleepValue.setText(textValue[2]);

        final TypefaceTextView txtVehicleHint = (TypefaceTextView) vehicleView.findViewById(R.id.settings_item_hint);
        txtVehicleHint.setText(textHint[0]);
        final TypefaceTextView txtAndroidHint = (TypefaceTextView) androidView.findViewById(R.id.settings_item_hint);
        txtAndroidHint.setText(textHint[1]);
        final TypefaceTextView txtSleepHint = (TypefaceTextView) alwaysOnScreenView.findViewById(R.id.settings_item_hint);
        txtSleepHint.setText(textHint[2]);

        final TypefaceTextView txtLess50Thresholds = (TypefaceTextView) thresholdsLess50View.findViewById(R.id.settings_edit_item_value);
        txtLess50Thresholds.setText(getString(R.string.less_thresholds));
        final EditText txtLess50ThresholdsHint = (EditText) thresholdsLess50View.findViewById(R.id.settings_edit_item_hint);

        txtLess50ThresholdsHint.setText(String.valueOf(less50Thresholds));

        final TypefaceTextView txtMore50Thresholds = (TypefaceTextView) thresholdsMore50View.findViewById(R.id.settings_edit_item_value);
        txtMore50Thresholds.setText(getString(R.string.more_thresholds));
        final EditText txtMore50ThresholdsHint = (EditText) thresholdsMore50View.findViewById(R.id.settings_edit_item_hint);

        txtMore50ThresholdsHint.setText(String.valueOf(more50Thresholds));

        final TypefaceTextView txtTimeInterval = (TypefaceTextView) timeIntervalView.findViewById(R.id.settings_item_value);
        txtTimeInterval.setText(getString(R.string.time_interval));
        final TypefaceTextView txtTimeIntervalHint = (TypefaceTextView) timeIntervalView.findViewById(R.id.settings_item_hint);
        txtTimeIntervalHint.setText(timeInterval.toString());
        final TypefaceTextView txtSuspensionHint = (TypefaceTextView) suspensionTypeView.findViewById(R.id.settings_item_hint);
        txtSuspensionHint.setTextColor(getAppResouces().getColor(R.color.type_issue_blue));
        switch(suspensionType) {
            case SOFT:
                txtSuspensionHint.setText(getString(R.string.soft));
                break;
            case MEDIUM:
                txtSuspensionHint.setText(getString(R.string.medium));
                break;
            case HARD:
                txtSuspensionHint.setText(getString(R.string.hard));
                break;
        }
        txtSuspensionType.setText(getString(R.string.suspension_type));
        suspensionTypeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SuspensionTypeDialog(getMainActivity(), new SettingsDialog.SettingsDialogListener() {
                    @Override
                    public void onRequestClose(int type) {
                        PreferencesUtil.getInstance(getMainActivity()).setSuspensionType(type);
                        RAApplication.getInstance().getIriTable().init();
                        switch (type) {
                            case 0: {
                                txtSuspensionHint.setText(getString(R.string.soft));
                                break;
                            }
                            case 1: {
                                txtSuspensionHint.setText(getString(R.string.medium));
                                break;
                            }
                            case 2: {
                                txtSuspensionHint.setText(getString(R.string.hard));
                                break;
                            }
                        }
                    }
                }).show();
            }
        });
        int syncDataTypeNameId = syncDataType.getNameId();
        txtSyncProviderHint.setText(getString(syncDataTypeNameId));
        syncProviderTypeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SyncProviderTypeDialog(getMainActivity(), new SettingsDialog.SettingsDialogListener() {
                    @Override
                    public void onRequestClose(int type) {
                        SyncDataType syncType = SyncDataType.values()[type];
                        PreferencesUtil.getInstance().setSyncProviderType(syncType);
                        int syncDataTypeNameId = syncType.getNameId();
                        String dataTypeName = getString(syncDataTypeNameId);
                        txtSyncProviderHint.setText(dataTypeName);
                        initSyncDataManager();
                        setLoginButton(true);
                        //dropboxLogout();
                    }
                }).show();
            }
        });

        txtLess50ThresholdsHint.addTextChangedListener(new TextWatcher() {
                                                           @Override
                                                           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                           }

                                                           @Override
                                                           public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                               try {
                                                                   final float tresholds = Float.parseFloat(s.toString());
                                                                   PreferencesUtil.getInstance(getMainActivity()).setLess50SpeedThresholds(tresholds);
                                                                   RAApplication.getInstance().getIriTable().init();
                                                               } catch (NumberFormatException e) {
                                                                   PreferencesUtil.getInstance(getMainActivity()).clearLess50SpeedThresholds();
                                                                   final float tresholds = PreferencesUtil.getInstance(getMainActivity()).getLess50SpeedThresholds();
                                                                   txtLess50ThresholdsHint.setText(String.valueOf(tresholds));
                                                               }
                                                           }

                                                           @Override
                                                           public void afterTextChanged(Editable s) {

                                                           }
                                                       }
        );

        txtMore50ThresholdsHint.addTextChangedListener(new TextWatcher() {
                                                           @Override
                                                           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                           }

                                                           @Override
                                                           public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                               try {
                                                                   final float tresholds = Float.parseFloat(s.toString());
                                                                   PreferencesUtil.getInstance(getMainActivity()).setMore50SpeedThresholds(tresholds);
                                                                   RAApplication.getInstance().getIriTable().init();
                                                               } catch (NumberFormatException e) {
                                                                   PreferencesUtil.getInstance(getMainActivity()).clearMore50SpeedThresholds();
                                                                   final float tresholds = PreferencesUtil.getInstance(getMainActivity()).getMore50SpeedThresholds();
                                                                   txtLess50ThresholdsHint.setText(String.valueOf(tresholds));
                                                               }
                                                           }

                                                           @Override
                                                           public void afterTextChanged(Editable s) {

                                                           }
                                                       }
        );

        timeIntervalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimeIntervalDialog(getMainActivity(), new SettingsDialog.SettingsDialogListener() {
                    @Override
                    public void onRequestClose(int type) {
                        PreferencesUtil.getInstance(getMainActivity()).setTimeInterval(type);
                        RAApplication.getInstance().getIriTable().init();
                        getIntervalsRecordHelper().reinit(getMainActivity(), false);
                        txtTimeIntervalHint.setText(PreferencesUtil.TIMEINTERVAL.values()[type].toString());
                    }
                }).show();
            }
        });

        final TypefaceTextView txtVehcile = (TypefaceTextView) vehicleView.findViewById(R.id.settings_item_value);
        final TypefaceTextView txtAndroid = (TypefaceTextView) androidView.findViewById(R.id.settings_item_value);

        txtVehcile.setText(PreferencesUtil.getInstance(getMainActivity()).getVehicle());
        txtAndroid.setText(DeviceUtil.findDeviceID(RAApplication.getInstance()));

        androidView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new EnterIDDialog(getMainActivity(), textValue[1], new EnterIDDialog.CustomInputDialogListener() {
                    @Override
                    public void onRequestClose(String value) {
                        PreferencesUtil.getInstance(getMainActivity()).setDeviceId(value);
                        txtAndroid.setText(value);
                    }
                }).show();
            }
        });

        vehicleView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new EnterIDDialog(getMainActivity(), textValue[0], new EnterIDDialog.CustomInputDialogListener() {
                    @Override
                    public void onRequestClose(String value) {
                        PreferencesUtil.getInstance(getMainActivity()).setVehicle(value);
                        txtVehcile.setText(value);
                    }
                }).show();
            }
        });


        final boolean flagFromPrefSleep = PreferencesUtil.getInstance(getMainActivity()).getSleepModeEnabled();
        final CheckBox checkAlwaysOnScreenMode = (CheckBox) alwaysOnScreenView.findViewById(R.id.settings_item_switch_mode);
        checkAlwaysOnScreenMode.setChecked(flagFromPrefSleep);
        checkAlwaysOnScreenMode.setVisibility(View.VISIBLE);
        checkAlwaysOnScreenMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isCheckAlwaysScreenMode = checkAlwaysOnScreenMode.isChecked();
                PreferencesUtil.getInstance(getMainActivity()).setAlwaysOnScreenModeEnabled(isCheckAlwaysScreenMode);
                setSleepingMode(isCheckAlwaysScreenMode);
            }
        });

/*        view.findViewById(R.id.fr_settings_btn_export_data).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportData();
            }
        });*/

        dropBoxLoadDataButton = (CustomButton) view.findViewById(R.id.fr_settings_btn_sync);
        dropBoxLoadDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    new ExportDialog(getMainActivity(), new SettingsDialog.SettingsDialogListener() {
                        @Override
                        public void onRequestClose(int type) {
                            PreferencesUtil.getInstance(getMainActivity()).setExportType(type);
                            switch (type) {
                                case 0: {
                                    exportData(null);
                                    break;
                                }
                                case 1: {
                                    startSynchronizationWithDropBox();
                                    break;
                                }
                            }
                        }
                    }).show();
//                startSynchronizationWithDropBox();
            }
        });

        view.findViewById(R.id.fr_settings_btn_reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!RAApplication.getInstance().isRecordStarted()) {
                    showDeleteWarningDlg();
                } else {
                    Toast.makeText(
                            getActivity(),
                            getString(R.string.toast_error_stop_tracking_data_please),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        dropBoxLoginButton = (CustomButton) view.findViewById(R.id.fr_settings_btn_dbx_login_logout);
        setLoginButton();
        dropBoxLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLoginToDropBox) {
                    dropboxLogout();
                } else {
                    login();
                }
            }
        });
    }

    private void login() {
        getDataSyncManager().login(getMainActivity(),
        new SyncDataManager.OnSyncDataListener<Boolean>() {
            @Override
            public void onComplete(Boolean data) {
                setLoginButton(true);
            }
        });
    }

    protected void showDeleteWarningDlg() {
        String msgStr = getString(R.string.delete_all_data_folder_str);
        CommonDialogs.buildAlertMessageDlg(getActivity(), msgStr, new CommonDialogs.OnOkListener() {
            @Override
            public void onDlgOkPressed() {
                resetSettings();
            }
        }).show();
    }

    private void resetSettings() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPostExecute(Void o) {
                super.onPostExecute(o);
                getGpsDetector().reset();
                getIntervalsRecordHelper().reinit(getMainActivity());
                Toast.makeText(getMainActivity(), getString(R.string.reset_all_data), Toast.LENGTH_LONG).show();
            }

            @Override
            protected Void doInBackground(Void[] params) {
                MeasurementsDataHelper.getInstance().deleteAllProjectsSync(true);
                FileUtils.clearAllRecordsDir();
                return null;
            }
        }.execute();
    }

    private void dropboxLogout() {
        getDataSyncManager().logout();
        isLoginToDropBox = getDataSyncManager().hasToken();
        if (!isLoginToDropBox) {
            setLoginButtonView();
            getMainActivity().checkUser();
        }
    }

    private void dropboxLoadData() {
        getDataSyncManager().loadData(new GetAccountCallback() {
            @Override
            public void onComplete(SyncDataType type, User result) {
                setLoginButton(true);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(RAApplication.getInstance().getBaseContext(), RAApplication.getInstance().
                getText(R.string.toast_failed_login_to_dbx), Toast.LENGTH_LONG).show();
                Log.e(getClass().getName(), "Failed to get account details.", e);
            }
        });
    }

    private void setLoginButton() {
        setLoginButton(false);
    }

    private void setLoginButton(boolean checkUser) {
        isLoginToDropBox = getDataSyncManager().hasToken();
        if (checkUser) {
            if (getMainActivity() != null) {
                getMainActivity().checkUser();
            }
        }
        setLoginButtonView();
    }

    private void setLoginButtonView() {
        SyncDataType dataType = PreferencesUtil.getInstance().getSyncProviderType();
        int syncDataTypeNameId = dataType.getNameId();
        String dataTypeName = getString(syncDataTypeNameId);
        String buttonText = "";
        if (isLoginToDropBox) {
            buttonText = getButtonTextLogoutStr(dataTypeName);
        } else {
            buttonText = getResources().getString(R.string.fr_settings_dbx_login, dataTypeName);
        }
        dropBoxLoadDataButton.setEnabled(true);
        buttonText.toUpperCase();
        dropBoxLoginButton.setText(buttonText);
    }

    private String getButtonTextLogoutStr(String dataTypeName) {
        String buttonText = getResources().getString(R.string.fr_settings_dbx_logout, dataTypeName);
        String userName = PreferencesUtil.getInstance().getAccountUserName();
        String userNameSuffix = "";
        if (!TextUtils.isEmpty(userName)) {
            userNameSuffix = getResources().getString(R.string.fr_settings_user_name, userName);
        }
        buttonText += " " + userNameSuffix;
        return buttonText;
    }

    private void setSleepingMode(final boolean enabled) {
        if (getMainActivity() != null) {
            getMainActivity().setSleepMode(enabled);
        }
    }

    private void checkRoughnessDetectionEnabled() {
        if (getMainActivity() != null) {
            getMainActivity().checkRoughnessDetectionEnabled();
        }
    }

    private void startSynchronizationWithDropBox() {
        if (getDataSyncManager().authentication()) {
            PreferencesUtil.getInstance().incExportId();
            String dataTypeName = PreferencesUtil.getInstance().getSyncProviderTypeName(getContext());
            new DropboxSyncDialog(getContext(),
                    getAppResouces().getString(R.string.dropbox_dialog_title, dataTypeName),
                    Constants.DROPBOX_DEFAULT_FOLDER + "/" +
                            TimeUtil.getFormattedDate(TimeUtil.DATE_DROPBOX_FOLDER_NAME_FORMAT),
                    new CustomInputDialog.CustomInputDialogListener() {
                        @Override
                        public void onRequestClose(String value) {
                            startDropboxToSelectedFolder(value);
                        }
                    }).show();
        } else {
            Toast.makeText(getMainActivity(), getMainActivity().getString(R.string.toast_login_to_dropbox_please), Toast.LENGTH_LONG).show();
        }
    }

    private void startDropboxToSelectedFolder(String path) {
        if (getDataSyncManager().isFolderAlreadyExists(path)) {
            Toast.makeText(
                    getActivity(),
                    getString(R.string.toast_error_folder_exist_in_dropbox_yet, path),
                    Toast.LENGTH_LONG
            ).show();
            startSynchronizationWithDropBox();
        } else {
            exportData(path);
        }
    }

    private void exportData(final String value) {
        wasCancelled = false;
        stopDataExport();
        showProgress(true);
        exportTask = new ExportMeasurementDB(getActivity(), new ExportToCSVResult() {
            @Override
            public void onResultsAfterExporting(String[] executed) {
                showProgress(false);
                if (!wasCancelled) {
                    if (value == null) {
                        Toast.makeText(
                                getMainActivity(),
                                getString(R.string.toast_export_to_device_success),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        getDataSyncManager().uploadFiles(value);
                    }
                }

            }
        });
        exportTask.execute("args");
    }

    @Override
    protected void showProgress(final boolean show) {
        if (progress == null) {
            progress = new ProgressDialog(getActivity());
            progress.setMessage(getString(R.string.export_all_progress_str));
            progress.setCancelable(false);
            progress.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        showCancelDialog();
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }
        if (show) {
            if (!progress.isShowing()) {
                progress.show();
            }
        } else {
            progress.dismiss();
        }
    }

    private void showCancelDialog() {
        if (alertToShow != null) {
            alertToShow.dismiss();
        }
        alertToShow = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.export_cancel_prompt_str)
                .setPositiveButton(R.string.ok_btn_txt, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        stopDataExport();
                        showProgress(false);
                        wasCancelled = true;
                    }
                }).setNegativeButton(R.string.cancel_btn_txt, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).create();
        alertToShow.show();
    }

    private void stopDataExport() {
        if (exportTask != null) {
            exportTask.stopProcess();
        }
        if (alertToShow != null) {
            alertToShow.dismiss();
        }
    }
}
