package com.adamwberck.android.makeareminder.Elements;

import org.joda.time.DateTime;

import java.util.List;

public class Group {
    private String mName = "Urgent";
    private Repeat mDefaultRepeat;
    private long mDefaultSnooze;
    private DateTime mDefaultTime;
    private List<Reminder> mDefaultReminders;

    private List<Task> mTasks;

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
}
