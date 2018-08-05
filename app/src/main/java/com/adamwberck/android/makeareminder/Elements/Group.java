package com.adamwberck.android.makeareminder.Elements;

import com.adamwberck.android.makeareminder.ReminderService;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Group implements Serializable{
    private UUID mID = UUID.randomUUID();
    private String mName = "Urgent";
    private Repeat mDefaultRepeat;
    private long mDefaultSnooze;
    private DateTime mDefaultTime;
    private List<Reminder> mDefaultReminders;

    private List<Task> mTasks;

    public int getTaskIndex(Task task) {
        return mTasks.indexOf(task);
    }

    public void addTask(Task task){
        mTasks.add(task);
        //TODO ensure saving

    }

    public boolean removeTask(Task t){
        //if(mTasks.remove(t)){
            //ReminderService.cancelServiceAlarm(mContext,t.getID());
            //TODO cancel alarms
        //}
        return mTasks.remove(t);
    }

    public List<Task> getTasks() {
        return mTasks;
    }

    public Task getTask(int id){
        for(Task t: mTasks){
            if(t.getID()==id){
                return t;
            }
        }
        return null;
    }


    public String getName() {
        return mName;
    }

    public int getDueToday() {
        int total = 0;
        for(Task t: mTasks){
            if(t.isDueToday()){
                total++;
            }
        }
        return total;
    }

    public int getOverdue() {
        int total = 0;
        for(Task t: mTasks){
            if(t.isOverdue()){
                total++;
            }
        }
        return total;
    }

    public UUID getID() {
        return mID;
    }

    public boolean removeTask(int taskId) {
        for(Task t: mTasks){
            if(t.getID()==taskId){
                return mTasks.remove(t);
            }
        }
        return false;
    }
}
