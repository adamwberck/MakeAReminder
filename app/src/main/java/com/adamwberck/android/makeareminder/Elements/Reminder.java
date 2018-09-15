package com.adamwberck.android.makeareminder.Elements;

import android.annotation.SuppressLint;
import android.content.Context;

import com.adamwberck.android.makeareminder.R;

import java.io.Serializable;
import java.util.Comparator;



public class Reminder implements Serializable {
    private static Comparator<Reminder> sComparator = new ReminderComparator();
    private Task mTask;
    private Group mGroup;
    private SpanOfTime mTimeBefore;
    private boolean mMatchesDefault;
    private boolean mIsAlarm ;
    private boolean mDoesVibrate;

    public Reminder(Task task,SpanOfTime duration, boolean isAlarm, boolean doesVibrate) {
        mTask = task;
        mTimeBefore = duration;
        mIsAlarm = isAlarm;
    }

    public Reminder(Task task, SpanOfTime duration) {
        mTask = task;
        mTimeBefore = duration;
        mMatchesDefault = true;

    }

    public Reminder(Group group, SpanOfTime duration) {
        mGroup = group;
        mTimeBefore = duration;
    }


    @Override
    public boolean equals(Object o){
        try {
            return getMinutes()==((Reminder)o).getMinutes();
        }
        catch (ClassCastException c){
            return false;
        }
    }

    public static Comparator<Reminder> getComparator() {
        return sComparator;
    }


    @SuppressLint("NewApi")
    public String getInfo(Context context) {
        if(mTimeBefore.getMinutes() == 0){
            return context.getString(R.string.when_due);
        }else {
            return getTimeBefore().getTimeString(context,"","before due.");
        }

    }


    public SpanOfTime getTimeBefore() {
        return mTimeBefore;
    }

    public long getMinutes() {
        return getTimeBefore().getMinutes();
    }

    public boolean isAlarm() {
        return mIsAlarm;
    }


    public long getMillis() {
        return mTimeBefore.getMillis();
    }


    public boolean doesVibrate() {
        return mDoesVibrate;
    }



    public void update(Reminder reminder){
        if(mMatchesDefault){
            mDoesVibrate=reminder.doesVibrate();
            mIsAlarm=reminder.isAlarm();
        }
    }

    public Task getTask() {
        return mTask;
    }

    private static class ReminderComparator implements Comparator<Reminder>,Serializable {
        @Override
        public int compare(Reminder r1, Reminder r2) {
            if(r1.getTimeBefore()!=r2.getTimeBefore()){
                return (r1.getMinutes() < r2.getMinutes()) ? 1 : -1;
            }else{
                return 0;
            }
        }
    }
}
