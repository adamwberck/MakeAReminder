package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;

import com.adamwberck.android.makeareminder.ReminderService;
import com.adamwberck.android.makeareminder.SortedObjectList;
import com.adamwberck.android.makeareminder.TaskLab;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Task implements Serializable{

    private int mID;
    private String mName = "";
    private DateTime mDate;
    private Repeat mRepeat;
    private boolean mHasRepeat = false;
    private List<Reminder> mReminders = new SortedObjectList<>(10,Reminder.getComparator());

    public void addReminder(Reminder r){
        mReminders.add(r);
        TaskLab.saveLab();
    }

    public Repeat getRepeat() {
        return mRepeat;
    }

    public void setRepeat(Repeat repeat) {
        mRepeat = repeat;
        TaskLab.saveLab();
    }

    public void addReminder(SpanOfTime span){
        addReminder(new Reminder(this,span));
    }

    public void removeReminder(Reminder r){
        mReminders.remove(r);
        TaskLab.saveLab();
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName,Context appContext) {
        this.mName = mName;
        TaskLab.saveLab();
        startAlarm(appContext);
    }

    public DateTime getDate() {
        return mDate;
    }

    public void setDate(DateTime date) {
        this.mDate = roundDate(date,1);
        addReminder(new Reminder(this,SpanOfTime.ofMinutes(0)));
        TaskLab.saveLab();
    }

    public void test(Context appContext){
        setDate(new DateTime().plusMinutes(3));
        addReminder(new Reminder(this,SpanOfTime.ofMinutes(2),true));
        startAlarm(appContext);
    }

    public void startAlarm(Context appContext){
        if(mName.isEmpty()){
            return;
        }
        if(mDate==null){
            return;
        }
        if(mDate.isBeforeNow()){
            return;
        }
        //TODO Fix this shit
        ReminderService.setServiceAlarm(appContext,mID,mName);
    }


    public int getID() {
        return mID;
    }

    public Task(Context appContext) {
        mID = TaskLab.get(appContext).nextValue();
    }


    private DateTime roundDate(final DateTime dateTime, final int minutes) {
        if (minutes < 1 || 60 % minutes != 0) {
            throw new IllegalArgumentException("minutes must be a factor of 60");
        }

        final DateTime hour = dateTime.hourOfDay().roundFloorCopy();
        final long millisSinceHour = new Duration(hour, dateTime).getMillis();
        final int roundedMinutes = ((int)Math.round(
                millisSinceHour / 60000.0 / minutes)) * minutes;
        return hour.plusMinutes(roundedMinutes);
    }

    @Override
    public boolean equals(Object o) {
        if(o.getClass()!=Task.class){
            return false;
        }
        Task t = (Task) o;
        return mID == t.mID;
    }

    public List<Reminder> getReminders() {
        return  mReminders;
    }

    public void addReminder(SpanOfTime span, boolean isAlarm) {
        Reminder r = new Reminder(this, span, isAlarm);
        addReminder(r);
    }

    public DateTime getSoonestTime() {
        if(mDate.isAfterNow()){
            for(int i=mReminders.size()-1;i<=0;i--){
                Reminder r = mReminders.get(i);
                DateTime time = new DateTime().minusMinutes((int)r.getMinutes());
                if(time.isAfterNow()){
                    return time;
                }
            }
        }
        return null;
    }
}
