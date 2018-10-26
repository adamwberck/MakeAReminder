package com.adamwberck.android.makeareminder.Elements;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.adamwberck.android.makeareminder.R;

import java.io.Serializable;
import java.util.Comparator;



public class Reminder implements Serializable {
    private static Comparator<Reminder> sComparator = new ReminderComparator();
    private int mInputTime;
    private Task mTask;
    private Group mGroup;
    private int mValue;
    transient private Uri mRingtoneUri;
    private float mVolume;
    private SpanOfTime mTimeBefore;
    private boolean mMatchesDefault;
    private boolean mIsAlarm = false;
    private boolean mDoesVibrate = true;

    public Reminder(Task task,SpanOfTime duration,int inputTime, boolean isAlarm,
                    boolean doesVibrate, Uri ringtoneUri, float volume) {
        mTask = task;
        mTimeBefore = duration;
        mInputTime = inputTime;
        mDoesVibrate = doesVibrate;
        mIsAlarm = isAlarm;
        mMatchesDefault = false;
        mRingtoneUri = ringtoneUri;
        RingtoneManager manager;
        mVolume = volume;
    }

    public Reminder(Task task, SpanOfTime duration, int inputTime) {
        mInputTime = inputTime;
        mTask = task;
        mTimeBefore = duration;
        mMatchesDefault = true;
    }

    public Reminder(Group group, SpanOfTime duration, int inputTime) {
        mInputTime = inputTime;
        mGroup = group;
        mTimeBefore = duration;
        mMatchesDefault = true;
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
        if(mMatchesDefault){
            return mTask.getBaseReminder().mIsAlarm;
        }
        return mIsAlarm;
    }


    public long getMillis() {
        return mTimeBefore.getMillis();
    }


    public boolean doesVibrate() {
        if(mMatchesDefault){
            return mTask.getBaseReminder().doesVibrate();
        }
        return mDoesVibrate;
    }



    public Task getTask() {
        return mTask;
    }

    public boolean matchesDefault() {
        return mMatchesDefault;
    }

    public int getValue() {
        return mValue;
    }

    public int getInputTime() {
        return mInputTime;
    }


    public Ringtone getRingtone(Context context) {
        if(!mMatchesDefault) {
            return RingtoneManager.getRingtone(context,mRingtoneUri);
        }
        return mTask.getBaseReminder().getRingtone(context);
    }

    public float getVolume() {
        return mVolume;
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
