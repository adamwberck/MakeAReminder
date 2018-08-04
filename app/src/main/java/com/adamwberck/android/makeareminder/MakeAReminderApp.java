package com.adamwberck.android.makeareminder;

import android.app.Application;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;
//TODO handle dual view
public class MakeAReminderApp extends Application {
    private static final String TAG = "MakeAReminderApp";
        public void onCreate() {
            super.onCreate();
            Log.i(TAG,"Application Created");
            JodaTimeAndroid.init(getApplicationContext());
            StartDayService.setServiceAlarm(getApplicationContext(),true);
        }
}
