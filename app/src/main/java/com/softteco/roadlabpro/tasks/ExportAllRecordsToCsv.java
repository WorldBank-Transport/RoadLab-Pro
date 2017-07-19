package com.softteco.roadlabpro.tasks;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.RAApplication;
import com.softteco.roadlabpro.sqlite.dao.RecordDAO;
import com.softteco.roadlabpro.sqlite.model.RecordModel;
import com.softteco.roadlabpro.util.FileUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by ppp on 04.05.2015.
 */
public class ExportAllRecordsToCsv extends AsyncTask<Object, Object, Object> {

    private OnExportAllRecordsDoneListener listener;
    private ExportDBIntoCSV exportTool;
    private RecordDAO recordDao;
    private final static int MAX_ARR_VALUE = 2;
    private volatile boolean stopProcess = false;

    public interface OnExportAllRecordsDoneListener {
        void onExportDone();

        void onUpdateProgress(String progressStr);
    }

    public ExportAllRecordsToCsv(final RecordDAO recordDao, final SQLiteDatabase sqLiteDatabase, final OnExportAllRecordsDoneListener listener) {
        this.recordDao = recordDao;
        this.exportTool = new ExportDBIntoCSV(sqLiteDatabase);
        setOnExportAllRecordsDoneListener(listener);
    }

    public void setOnExportAllRecordsDoneListener(final OnExportAllRecordsDoneListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        if (listener != null && values != null && values.length == MAX_ARR_VALUE) {
            listener.onUpdateProgress(RAApplication.getInstance().
                    getString(R.string.export_all_progress_info_str, (int) values[0], (int) values[1]));
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    public void stopProcess() {
        stopProcess = true;
        cancel(true);
    }

    @Override
    protected Object doInBackground(Object[] params) {


        stopProcess = false;
        List<RecordModel> recordList = recordDao.getAllRecords();
        Date date = null;
        String allRecordsDir = FileUtils.clearAllRecordsDir();
        int count = 0;
        publishProgress(count, recordList.size());
        for (RecordModel r : recordList) {
            if (stopProcess) {
                return null;
            }
            date = new Date();
            exportTool.writeHeadFile(r, date, allRecordsDir);
            exportTool.writeDetailsFile(r, date, allRecordsDir);
            count++;
            publishProgress(count, recordList.size());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (listener != null) {
            listener.onExportDone();
        }
    }
}
