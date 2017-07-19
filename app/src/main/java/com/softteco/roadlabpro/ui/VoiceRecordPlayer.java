package com.softteco.roadlabpro.ui;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.softteco.roadlabpro.util.FileUtils;

import java.io.IOException;

public class VoiceRecordPlayer {

    private static final String TAG = "VoiceRecordPlayer";

    private String mFileFullName = null;
    private String mFileName = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private boolean playingRecord;
    private VoicePlayerListener listener;

    public interface VoicePlayerListener {
        void onRecord(boolean started);
        void onPlay(boolean started);
        void onPaused();
        void onCompleted();
    }

    public VoiceRecordPlayer(String fileName) {
        this.mFileName = fileName;
        init();
    }

    public void setListener(VoicePlayerListener listener) {
        this.listener = listener;
    }

    private void init() {
        playingRecord = false;
        mFileFullName = FileUtils.getAudioDir() + mFileName;
    }

    public String getFileFullName() {
        return mFileFullName;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    public void setFileFullName(String mFileFullName) {
        this.mFileFullName = mFileFullName;
    }

    public boolean isPlayingRecord() {
        return playingRecord;
    }

    public void onRecord(boolean start) {
        if (start) {
            startRecording();
            if (listener != null) {
                listener.onRecord(true);
            }
        } else {
            stopRecording();
            if (listener != null) {
                listener.onRecord(false);
            }
        }
    }

    public void onPlay(boolean start) {
        if (start) {
            if (playingRecord) {
                pausePlaying();
                if (listener != null) {
                    listener.onPaused();
                }
            } else {
                startPlaying();
                if (listener != null) {
                    listener.onPlay(true);
                }
            }
            playingRecord = !playingRecord;
        } else {
            stopPlaying();
            if (listener != null) {
                listener.onPlay(false);
            }
        }
    }

    public void startPlaying() {
        mPlayer = new MediaPlayer();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("VOICE", "Ended");
                playingRecord = false;
                if (listener != null) {
                    listener.onCompleted();
                }
            }
        });
        try {
            mPlayer.setDataSource(mFileFullName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    public void pausePlaying() {
        try {
            if (mPlayer != null) {
                mPlayer.pause();
            }
        } catch (Exception e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    public void stopPlaying() {
        try {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    public void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        String filePath = FileUtils.createFile(mFileFullName);
        mRecorder.setOutputFile(filePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            Log.e(TAG, "prepare() failed");
        }
    }

    public void stopRecording() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (Exception e) {
            Log.d(TAG, "stopRecording", e);
        }
    }

    public void onStop() {
        try {
            if (mRecorder != null) {
                mRecorder.release();
                mRecorder = null;
            }
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
        } catch (Exception e) {
            Log.d(TAG, "stopRecording", e);
        }
    }
}
