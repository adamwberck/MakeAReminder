package com.adamwberck.android.makeareminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adamwberck.android.makeareminder.Elements.Task;

import java.util.List;

public class StartupReceiver extends BroadcastReceiver {
    private static final String TAG =
            "StartupReceiver";
    @Override
    public void onReceive(Context context, Intent
            intent) {
            List<Task> tasks = TaskLab.get(context).getTasks();
            for(Task task:tasks) {
                ReminderService.setServiceAlarm(context,task.getID(),task.getName(),
                        true);
            }
    }
}
