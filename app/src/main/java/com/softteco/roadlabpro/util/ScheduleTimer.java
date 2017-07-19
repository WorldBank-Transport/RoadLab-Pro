package com.softteco.roadlabpro.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ppp on 17.04.2015.
 */
public class ScheduleTimer {

    private long timerPeriod;
    private Timer timer;
    private TimerTaskListener listener;

    public interface TimerTaskListener {
        void onTimer();
    }

    public ScheduleTimer(long timerPeriod) {
        setTimerPeriod(timerPeriod);
    }

    public void setTimerPeriod(long timerPeriod) {
        this.timerPeriod = timerPeriod;
    }

    private void schedule() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onTimer();
                }
            }
        }, 0, timerPeriod);
    }

    public void start() {
        stop();
        schedule();
    }

    public void stop() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
    }

    public void setOnTimerTaskListener(TimerTaskListener listener) {
        this.listener = listener;
    }
}
