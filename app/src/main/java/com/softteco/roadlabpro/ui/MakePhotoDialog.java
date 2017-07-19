package com.softteco.roadlabpro.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.softteco.roadlabpro.R;

public class MakePhotoDialog extends Dialog {

    private MakePhotoListener listener;

    public MakePhotoDialog(final Context context, final MakePhotoListener photoListener) {
        super(context);
        listener = photoListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_report_photo);

        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);

        findViewById(R.id.dlg_report_photo_choose_new_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRequestOpenGallery();
                dismiss();
            }
        });

        findViewById(R.id.dlg_report_photo_take_new_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRequestOpenCamera();
                dismiss();
            }
        });
    }

    public interface  MakePhotoListener {
        void onRequestOpenCamera();
        void onRequestOpenGallery();
    }
}
