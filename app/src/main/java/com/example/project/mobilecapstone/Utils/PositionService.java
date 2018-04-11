package com.example.project.mobilecapstone.Utils;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;

/**
 * Created by admin on 3/4/2018.
 */

public class PositionService extends IntentService {
    public PositionService() {
        super("PositionService");
    }
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while (true) {
            try{
                Thread.sleep(10000);
                new CreatePosition(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
