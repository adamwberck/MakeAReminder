package com.adamwberck.android.makeareminder;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.Task;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.Date;
import java.util.Locale;

public class ReminderService extends IntentService{
    //TODO start on startup
    //TODO switch so it uses reminders rather than tasks
    //TODO naming tasks nothing needs to be fixed problem everywhere
    private static final String TAG = "ReminderService";
    private static final String EXTRA_TASK_ID =  "com.adamwberck.android.makeareminder.taskid";
    private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";
    private static final String EXTRA_NAME = "com.adamwberck.android.makeareminder.name";
    private static final String EXTRA_TITLE = "com.adamwberck.android.makeareminder.title";

    public ReminderService() {
        super(TAG);
    }
    public static void cancelServiceAlarm(Context context,int id){
        Intent i = ReminderService.newIntent(context);
        i.putExtra(EXTRA_TASK_ID,id);
        int requestCode = id;
        cancel(context, i,requestCode);
    }
    public static void setServiceAlarm(Context context, int id, String name,boolean turnAlarmOn) {
        Intent i = ReminderService.newIntent(context);
        i.putExtra(EXTRA_TASK_ID,id);
        int requestCode = id;
        if(!turnAlarmOn){
            cancel(context, i,requestCode);
        }
        else {
            Task task = TaskLab.get(context).getTask(id);
            Object[] os = task.getSoonestTime();
            if(os!=null) {
                DateTime soon = (DateTime) os[0];
                Reminder r = (Reminder) os[1];
                i.putExtra(EXTRA_NAME,name);
                if(r.getMinutes()!=0) {
                    String s = r.getTimeBefore().getTimeString(context,"",
                            " before due");
                    name = context.getString(R.string.alarm_reminder, name, s);
                }
                i.putExtra(EXTRA_TITLE,name);
                PendingIntent pi = PendingIntent.getService(context, requestCode, i,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                setExact(AlarmManager.RTC_WAKEUP, soon.getMillis(), pi, context, turnAlarmOn);
            }
            else if(task.getSnoozeTime()!=null){
                //TODO check if snooze is after repeat
                if(task.getSnoozeTime().isAfterNow()) {

                    i.putExtra(EXTRA_NAME, name);
                    i.putExtra(EXTRA_TITLE, name + " Originally due " + task.getDate()
                            .toString("hh:mm a"));
                    PendingIntent pi = PendingIntent.getService(context, requestCode, i,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    setExact(AlarmManager.RTC_WAKEUP, task.getSnoozeTime().getMillis(), pi, context,
                            turnAlarmOn);
                }
            }
            else if(task.getRepeat().isMoreOften()) {
                LocalTime lt = task.getRepeat().getSoonestTime();
                if(lt!=null){
                    DateTime dt = lt.toDateTimeToday();
                    task.setDate(dt);
                }
            }
            else {
                cancel(context, i,requestCode);
            }
        }

    }

    private static void cancel(Context context, Intent i,int requestCode) {
        PendingIntent pi = PendingIntent.getService(context, requestCode, i,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        pi.cancel();
    }

    private static void setExact(int rtcWakeup, long millis, PendingIntent pi,Context context,
                                 boolean turnAlarmOn) {
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
        String title = intent.getExtras().getString(EXTRA_TITLE);
        Intent i = AlarmActivity.newIntent(this,id,true,name,title);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
    }
}
