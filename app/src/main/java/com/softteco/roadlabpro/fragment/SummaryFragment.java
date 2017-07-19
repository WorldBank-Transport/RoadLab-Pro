package com.softteco.roadlabpro.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.menu.ScreenItems;
import com.softteco.roadlabpro.tasks.ExportMeasurementDB;
import com.softteco.roadlabpro.util.Constants;
import com.softteco.roadlabpro.util.DeviceUtil;
import com.softteco.roadlabpro.util.EMailUtil;
import com.softteco.roadlabpro.util.ExportToCSVResult;
import com.softteco.roadlabpro.util.ValidateUtil;

public class SummaryFragment extends BaseTabFragment {

    private MenuItem exportItem;
    private AlertDialog alertToShow;
    private ExportMeasurementDB exportTask;
    private boolean wasCancelled;

    public interface OnDeviceFixedListener {
        void onDeviceFixed(boolean isDeviceFixed);
    }

    private BroadcastReceiver deviceFixedBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean deviceFixed = false;
            if (Constants.ACTION_DEVICE_NOT_FIXED.equals(action)) {
                deviceFixed = false;
            } else if (Constants.ACTION_DEVICE_FIXED.equals(action)) {
                deviceFixed = true;
            }
            sendDeviceFixedEvent(deviceFixed);
        }
    };

    private void sendDeviceFixedEvent(boolean isDeviceFixed) {
        Fragment fragment = getCurrentFragment();
        if (fragment != null && fragment instanceof SummaryFragment.OnDeviceFixedListener) {
            ((SummaryFragment.OnDeviceFixedListener) fragment).onDeviceFixed(isDeviceFixed);
        }
    }

    public static SummaryFragment newInstance() {
        SummaryFragment fragment;
        fragment = new SummaryFragment();
        return fragment;
    }

    public SummaryFragment() {
        //
    }

    @Override
    protected void checkArgs() {
    }

    @Override
    protected void initTabs(FragmentTabHost mTabHost) {
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.measurements_tab_info_name))
                .setIndicator(getResources().getString(R.string.measurements_tab_info_name)), StartMeasurementFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec(getResources().getString(R.string.measurements_tab_map_name))
                .setIndicator(getResources().getString(R.string.measurements_tab_map_name)), StartMeasurementMapFragment.class, null);
        mTabHost.setCurrentTab(0);
    }

    public void setDeviceFixedBroadcast() {
        if (deviceFixedBroadcast != null) {
            getMainActivity().registerReceiver(deviceFixedBroadcast, new IntentFilter(Constants.ACTION_DEVICE_FIXED));
            getMainActivity().registerReceiver(deviceFixedBroadcast, new IntentFilter(Constants.ACTION_DEVICE_NOT_FIXED));
        }
    }

    @Override
    public void refresh() {
        Fragment fragment = getCurrentFragment();
        if (fragment != null && fragment instanceof AbstractWBFragment) {
            ((AbstractWBFragment) fragment).refresh();
        }
    }

    public void removeDeviceFixedBroadcast() {
        if (deviceFixedBroadcast != null && getMainActivity() != null) {
            getMainActivity().unregisterReceiver(deviceFixedBroadcast);
            deviceFixedBroadcast = null;
        }
    }

    @Override
    public int getMenuFragmentResources() {
        return R.menu.abc_menu_export_data;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (menu != null) {
            exportItem = menu.findItem(R.id.action_bar_export);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_bar_export:
                exportData();
                break;
            default:
                break;
        }
        return true;
    }

    private void exportData() {
        wasCancelled = false;
        stopDataExport();
        showProgress(true);
        exportTask = new ExportMeasurementDB(getMainActivity(),new ExportToCSVResult() {
            @Override
            public void onResultsAfterExporting(String[] executed) {
                showProgress(false);
                if (!wasCancelled) {
                    emailAfterExporting(executed);
                }
            }
        });
        exportTask.execute();
    }

    private void stopDataExport() {
        if (exportTask != null) {
            exportTask.stopProcess();
        }
        if (alertToShow != null) {
            alertToShow.dismiss();
        }
    }

    @Override
    protected void showProgress(final boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.export_all_progress_str));
            dialog.setCancelable(false);
            dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
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
            if (!dialog.isShowing()) {
                dialog.show();
            }
        } else {
            dialog.dismiss();
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

    public void emailAfterExporting(final String[] path) {
        if (!ValidateUtil.isNull(path)) {
            EMailUtil.sendAttachFileMail(
                    getActivity(),
                    path,
                    getActivity().getString(R.string.print_send_email_subject) + " " + DeviceUtil.getDeviceName(),
                    "text/plain");
            return;
        }
        Toast.makeText(getActivity(), "Export failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showProgress(false);
        stopDataExport();
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        setDeviceFixedBroadcast();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        removeDeviceFixedBroadcast();
    }

    @Override
    public int getLayoutFragmentResources() {
        return R.layout.fragment_summary;
    }

    @Override
    public int getTypeFragment() {
        return ScreenItems.SCREEN_MEASUREMENT;
    }

    @Override
    public boolean isHomeIndicatorMenu() {
        return true;
    }
}
