package com.adamwberck.android.makeareminder;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.adamwberck.android.makeareminder.Elements.Task;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class StartDayService extends IntentService {
    //public static final int ALARM_PERIOD = (int) TimeUnit.HOURS.toMillis(1);
    //for testing
    private static final String TAG = "StartDayService";
    public static final long ALARM_PERIOD = TimeUnit.DAYS.toMillis(1);
    public static final int REQUEST_CODE = Integer.MAX_VALUE-1;

    public StartDayService() {
        super(TAG);
    }


    public static void testServiceAlarm(Context context){
        Intent i = StartDayService.newIntent(context);
        Log.i(TAG,"Test Now");
        context.startService(i);
    }

    public static void setServiceAlarm(Context context, boolean turnAlarmOn) {
        Intent i = StartDayService.newIntent(context);

        boolean alarmUp =(PendingIntent.getBroadcast(context, REQUEST_CODE,
                StartDayService.newIntent(context),
                PendingIntent.FLAG_NO_CREATE) != null);
        Log.i(TAG,"Service Set");
        if(!turnAlarmOn){
            cancel(context, i);
        }
        else {
            if(!alarmUp) {
                Log.i(TAG,"Turning Start Alarm On");
                AlarmManager alarmManager = (AlarmManager)
                        context.getSystemService(Context.ALARM_SERVICE);

                long startTime = GroupLab.get(context).getStartDay();
                long cTime = startTime - new DateTime().getMillis();
                Log.i(TAG,"Seconds left: " + TimeUnit.MILLISECONDS.toSeconds(cTime));
                PendingIntent pi = PendingIntent.getService(context, REQUEST_CODE, i,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTime,
                //        60000, pi);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP,startTime,pi);
            }
        }
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, StartDayService.class);
    }


    private static void cancel(Context context, Intent i) {
        PendingIntent pi = PendingIntent.getService(context, REQUEST_CODE, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        pi.cancel();
    }

    @Override
    public void onHandleIntent(Intent intent){
        Log.i(TAG,"Start Intent Handled");
        Context context = getApplicationContext();
        List<Task> tasks = GroupLab.get(context).getTasks();
        for(Task task:tasks) {
            if(task.hasRepeat()){
                task.applyRepeat();
            }
            ReminderService.setServiceAlarm(context,task.getID(),task.getName(),
                    true);
        }
    }
}
