package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.os.Build;

import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class TaskLab {
    //public static final long MINUTE = 60000;

    private static TaskLab sTaskLab;
    private final Context mContext;
    private List<Task> mTasks;
    private SpanOfTime mDefaultSnooze;


    public static TaskLab get(Context context) {
        if (sTaskLab == null) {
            sTaskLab = new TaskLab(context);
        }
        return sTaskLab;
    }

    private TaskLab(Context context) {
        mContext = context.getApplicationContext();
        mTasks = new ArrayList<Task>();

    }

    public int getTaskIndex(Task task) {
        return mTasks.indexOf(task);
    }

    public void addTask(Task r){
        mTasks.add(r);
    }

    public void updateTask(Task task) {
        /*
        int loc = mTasks.indexOf(task);
        mTasks.remove(loc);
        mTasks.add(loc,task);*/
    }

    public void removeTask(Task r){
        mTasks.remove(r);
    }


    public List<Task> getTasks() {
        return mTasks;
    }

    public Task getTask(UUID uuid){
        for(Task r: mTasks){
            if(r.getID().equals(uuid)){
                return r;
            }
        }
        return null;
    }
}
