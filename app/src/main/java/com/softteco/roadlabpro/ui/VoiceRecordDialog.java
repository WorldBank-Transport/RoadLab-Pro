package com.softteco.roadlabpro.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.softteco.roadlabpro.R;
import com.softteco.roadlabpro.view.CircleImageView;
import com.softteco.roadlabpro.view.CustomButton;
import com.softteco.roadlabpro.view.TypefaceTextView;

/**
 * Created by bogdan on 20.04.2016.
 */
public class VoiceRecordDialog extends Dialog
    implements View.OnClickListener, VoiceRecordPlayer.VoicePlayerListener {

    protected VoiceRecordDialogListener listener;
    protected String title;

    private CustomButton saveButton;
    private CircleImageView startRecord;
    private CircleImageView stopRecord;
    private CircleImageView playRecord;
    private ImageView voiceRecordStop;
    private ImageView voiceRecordPlay;

    private static final String LOG_TAG = "AudioRecordTest";
    private VoiceRecordPlayer voicePlayer;
    private String mFileName;

    public VoiceRecordDialog(final Context context, final String titleDialog,
                             final String fileName, final VoiceRecordDialogListener photoListener) {
        super(context);
        listener = photoListener;
        title = titleDialog;
        mFileName = fileName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_voice_record);
        setCancelable(true);
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        initViews();
        initListeners();
        voicePlayer = new VoiceRecordPlayer(mFileName);
        voicePlayer.setListener(this);
        final TypefaceTextView txtTitle = (TypefaceTextView) findViewById(R.id.dlg_voice_record_header_title);
        txtTitle.setText(title);
        findViewById(R.id.dlg_voice_record_id_btn_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRequestClose(mFileName);
                dismiss();
            }
        });
        findViewById(R.id.dlg_voice_record_id_btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public interface VoiceRecordDialogListener {
        void onRequestClose(String value);
    }

    private void initViews() {
        saveButton = (CustomButton) findViewById(R.id.dlg_voice_record_id_btn_save);
        startRecord = (CircleImageView) findViewById(R.id.dialog_voice_start_record);
        stopRecord = (CircleImageView) findViewById(R.id.dialog_voice_stop_record);
        playRecord = (CircleImageView) findViewById(R.id.dialog_voice_play_record);
        voiceRecordStop = (ImageView) findViewById(R.id.voice_record_stop);
        voiceRecordPlay = (ImageView) findViewById(R.id.voice_record_play);

        saveButton.setEnabled(false);

        startRecord.setVisibility(View.VISIBLE);
        stopRecord.setVisibility(View.INVISIBLE);
        playRecord.setVisibility(View.INVISIBLE);
        voiceRecordStop.setVisibility(View.INVISIBLE);
    }

    private void initListeners() {
        startRecord.setOnClickListener(this);
        stopRecord.setOnClickListener(this);
        playRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_voice_start_record:
                startRecord.setVisibility(View.INVISIBLE);
                stopRecord.setVisibility(View.VISIBLE);
                if (voicePlayer != null) {
                    voicePlayer.onRecord(true);
                }
                break;
            case R.id.dialog_voice_stop_record:
                stopRecord.setVisibility(View.INVISIBLE);
                playRecord.setVisibility(View.VISIBLE);
                if (voicePlayer != null) {
                    voicePlayer.onRecord(false);
                }
                break;
            case R.id.dialog_voice_play_record:
                if (voicePlayer != null) {
                    voicePlayer.onPlay(true);
                }
                break;
        }
    }

    @Override
    public void onRecord(boolean started) {
        if (!started) {
            setStopRecordingUI();
        }
    }

    @Override
    public void onPlay(boolean started) {
        if (started) {
            setPlayingUI();
        }
    }

    @Override
    public void onPaused() {
        setPausePlayingUI();
    }

    @Override
    public void onCompleted() {
        setCompletedUI();
    }

    private void setStopRecordingUI() {
        saveButton.setEnabled(true);
    }

    private void setCompletedUI() {
        voiceRecordPlay.setVisibility(View.VISIBLE);
        voiceRecordStop.setVisibility(View.INVISIBLE);
    }

    private void setPlayingUI() {
        voiceRecordPlay.setVisibility(View.INVISIBLE);
        voiceRecordStop.setVisibility(View.VISIBLE);
    }

    private void setPausePlayingUI() {
        voiceRecordPlay.setVisibility(View.VISIBLE);
        voiceRecordStop.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (voicePlayer != null) {
            voicePlayer.onStop();
        }
    }
}
