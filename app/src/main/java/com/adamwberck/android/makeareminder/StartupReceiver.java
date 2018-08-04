package com.adamwberck.android.makeareminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adamwberck.android.makeareminder.Elements.Task;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.List;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG =
            "StartupReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Startup Receiver");
        List<Task> tasks = TaskLab.get(context).getTasks();
        //TODO Startup not showing in log
        for(Task task:tasks) {
            if(task.isComplete()&&task.hasRepeat()){
                task.applyRepeat();
            }
            ReminderService.setServiceAlarm(context,task.getID(),task.getName(),
                    true);
        }
        StartDayService.setServiceAlarm(context,true);
    }
}
