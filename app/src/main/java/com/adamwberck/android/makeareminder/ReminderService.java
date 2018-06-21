package com.adamwberck.android.makeareminder;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.adamwberck.android.makeareminder.Elements.Task;

import org.joda.time.DateTime;

public class ReminderService extends IntentService{
    //TODO start on startup
    //TODO switch so it uses reminders rather than tasks
    //TODO naming tasks nothing needs to be fixed problem everywhere
    private static final String TAG = "ReminderService";
    private static final String EXTRA_TASK =  "com.adamwberck.android.makeareminder.task";
    private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";

    public ReminderService() {
        super(TAG);
    }

    public static void setServiceAlarm(Context context, Task task) {
        Intent i = ReminderService.newIntent(context);
        i.putExtra(EXTRA_TASK,task);
        int requestCode = task.getID();
        PendingIntent pi = PendingIntent.getService(context, requestCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        DateTime dateTime = new DateTime(task.getDate());
        long millis = dateTime.getMillis();
        setExact(AlarmManager.RTC_WAKEUP,millis,pi,context);

    }

    private static void setExact(int rtcWakeup, long millis, PendingIntent pi,Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(rtcWakeup,millis,pi);
        }
        else {
            alarmManager.setExact(rtcWakeup,millis,pi);
        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, ReminderService.class);
    }

    @Override
    public void onHandleIntent(Intent intent){
        Task task = (Task) intent.getExtras().getSerializable(EXTRA_TASK);
        Intent i = OverviewActivity.newIntent(this,task.getID(),true);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(i);
    }
}
