package com.adamwberck.android.makeareminder;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.adamwberck.android.makeareminder.Elements.Task;

import org.joda.time.DateTime;

import java.util.Date;

public class ReminderService extends IntentService{
    //TODO start on startup
    //TODO switch so it uses reminders rather than tasks
    //TODO naming tasks nothing needs to be fixed problem everywhere
    private static final String TAG = "ReminderService";
    private static final String EXTRA_TASK_ID =  "com.adamwberck.android.makeareminder.taskid";
    private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";
    private static final String EXTRA_NAME = "com.adamwberck.android.makeareminder.name";

    public ReminderService() {
        super(TAG);
    }

    public static void setServiceAlarm(Context context, int id, String name) {
        Intent i = ReminderService.newIntent(context);
        i.putExtra(EXTRA_TASK_ID,id);
        i.putExtra(EXTRA_NAME,name);
        int requestCode = id;
        PendingIntent pi = PendingIntent.getService(context, requestCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Task task = TaskLab.get(context).getTask(id);
        Date date = task.getDate();
        long millis = new DateTime(date).getMillis();
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
        int id = intent.getExtras().getInt(EXTRA_TASK_ID);
        String name = intent.getExtras().getString(EXTRA_NAME);
        Intent i = OverviewActivity.newIntent(this,id,true,name);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(i);
    }
}
