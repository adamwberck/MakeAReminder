package com.adamwberck.android.makeareminder.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnBootReceiver extends BroadcastReceiver {
    private static final String TAG = "OnBootReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"On boot received.");
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, ReminderService.class );
        PendingIntent pi=PendingIntent.getBroadcast(context, 0, i, 0);
        /*
        mgr.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),pi);*/
    }

}
