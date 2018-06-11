package de.hhn.aib3.aufg3.gruppe11.utility;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Runs timer which sets off an event at specified intervals
 */
public class UpdateService extends Service {

    private final IBinder myBinder = new MyLocalBinder();
    private static final int DEFAULT_TIMER_INTERVAL = 1000;

    private Timer timer = null;

    private static final String DEBUGLOG_TAG = "DEBUGLOG-US";

    public UpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyLocalBinder extends Binder {
        public UpdateService getService() {
            return UpdateService.this;
        }
    }

    public void start(int timeIntervalInMills) {

        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        timer = new Timer();
        Log.d(DEBUGLOG_TAG, "Time interval:" + timeIntervalInMills);
        TimeDisplayTimerTask timeDisplayTimerTask = new TimeDisplayTimerTask();
        timer.schedule(timeDisplayTimerTask, 0, timeIntervalInMills);
    }

    /**
     * Start timer with custom timer interval
     *
     * @param timeIntervalInMills
     * @param context
     */
    public void start(int timeIntervalInMills, Context context) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        Log.d(DEBUGLOG_TAG, "Time interval:" + timeIntervalInMills);
        TimeDisplayTimerTask timeDisplayTimerTask = new TimeDisplayTimerTask(context);
        timer.schedule(timeDisplayTimerTask, 0, timeIntervalInMills);
    }

    /**
     * Start timer with default time interval
     *
     * @param context
     */
    public void start(Context context) {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        Log.d(DEBUGLOG_TAG, "Default Interval 1000ms");
        TimeDisplayTimerTask timeDisplayTimerTask = new TimeDisplayTimerTask(context);
        timer.schedule(timeDisplayTimerTask, 0, DEFAULT_TIMER_INTERVAL);
    }

    public void stop() {
        Log.d(DEBUGLOG_TAG, "stopping timer");
        timer.cancel();
        timer.purge();
    }

    private static class TimeDisplayTimerTask extends TimerTask {
        private Context context = null;

        private TimeDisplayTimerTask() {
            super();
        }

        private TimeDisplayTimerTask(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void run() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (context != null) {
                        EventBus.getDefault().post(new UpdateEvent(context));
                    } else {
                        EventBus.getDefault().post(new UpdateEvent());
                    }
                }
            });
        }
    }
}
