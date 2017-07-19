package com.softteco.roadlabpro.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.sqlite.MeasurementsDataHelper;
import com.softteco.roadlabpro.sqlite.dao.FolderDAO;
import com.softteco.roadlabpro.sqlite.model.FolderModel;
import com.softteco.roadlabpro.util.DateUtil;
import com.softteco.roadlabpro.util.DistanceUtil;

import java.util.Date;

public class FoldersAdapter extends CursorAdapter {

    private FolderDAO folderDAO;

    public FoldersAdapter(Context context, Cursor c, int flags, FolderDAO dao) {
        super(context, c, flags);
        this.folderDAO = dao;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.folder_list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final TextView folderName = (TextView) view.findViewById(R.id.folder_list_title);
        final TextView folderDetails = (TextView) view.findViewById(R.id.folder_list_details);
        final TextView txtDate = (TextView) view.findViewById(R.id.folder_list_date);
        final ImageView imageView = (ImageView) view.findViewById(R.id.folder_list_item_image);
        final FolderModel model = folderDAO.cursorToFolder(cursor);
        folderName.setText(model.getName());
        String distanceStr = MeasurementsDataHelper.
               getDistanceStrHtml(context, model.getOverallDistance(), model.getPathDistance());
        String detailsStr = context.getResources().
               getString(R.string.list_item_folder_details, model.getRoads(), distanceStr);
        folderDetails.setText(Html.fromHtml(detailsStr));
        txtDate.setText(DateUtil.format(new Date(model.getDate()), DateUtil.Format.DDMMYYY));
        imageView.setVisibility(model.isUploaded() ? View.VISIBLE : View.INVISIBLE);
    }

    private void setImage(Context context, ImageView image, Bitmap bmp) {
        if (bmp != null) {
            int MAX_SIZE = (int) context.getResources().getDimension(R.dimen.max_bitmap_size);
            image.setImageBitmap(Bitmap.createScaledBitmap(bmp, MAX_SIZE, MAX_SIZE, false));
        }
    }

    private void updateFolder(FolderModel folderModel) {
        new AsyncTask<FolderModel, Void, Void>() {
            @Override
            protected Void doInBackground(FolderModel... params) {
                folderDAO.updateFolder(params[0]);
                return null;
            }
        }.execute(folderModel);
    }

    @Override
    public int getItemViewType(int position) {
        Cursor c = (Cursor) getItem(position);
        final FolderModel model = folderDAO.cursorToFolder(c);
        int type = 0;
        if (model != null) {
            if (model.isDefaultProject()) {
                type = 1;
            }
        }
        return type;
    }

    @Override
    public void changeCursor(Cursor cursor) {
        super.changeCursor(cursor);
    }
}