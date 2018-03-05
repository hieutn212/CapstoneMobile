package com.example.project.mobilecapstone.Utils;

import android.app.IntentService;
import android.content.Intent;

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
                Thread.sleep(30000);
                new CreatePosition(this).execute();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
