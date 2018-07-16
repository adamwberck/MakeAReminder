package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;

import com.adamwberck.android.makeareminder.ReminderService;
import com.adamwberck.android.makeareminder.SortedObjectList;
import com.adamwberck.android.makeareminder.TaskLab;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Task implements Serializable{

    private int mID;
    private String mName = "";
    //TODO split into LocalTime and LocalDate
    private DateTime mDate;
    private Repeat mRepeat;
    private DateTime mSnoozeTime;
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
        mHasRepeat=true;
    }

    public void addReminder(SpanOfTime span){
        addReminder(new Reminder(this,span));
    }

    public void removeReminder(Reminder r){
        mReminders.remove(r);
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
        TaskLab.saveLab();
    }

    public DateTime getDate() {
        return mDate;
    }

    public void setDate(DateTime date) {
        this.mDate = floorDate(date,1);
        addReminder(new Reminder(this,SpanOfTime.ofMinutes(0)));
        String s = mDate.toString("hh:mm a", Locale.getDefault());
        TaskLab.saveLab();
    }

    public void test(){
        setDate(new DateTime().plusMinutes(60));
        //addReminder(new Reminder(this,SpanOfTime.ofMinutes(2),true));
        String id = Math.abs(mID)+"";
        id = id.substring(Math.min(0,id.length()-7));
        setName(id);
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
        ReminderService.setServiceAlarm(appContext,mID,mName,false);
        ReminderService.setServiceAlarm(appContext,mID,mName,true);
    }


    public int getID() {
        return mID;
    }

    public Task(Context appContext) {
        mID = TaskLab.get(appContext).nextValue();
    }


    private DateTime floorDate(final DateTime dateTime, final int minutes) {
        if (minutes < 1 || 60 % minutes != 0) {
            throw new IllegalArgumentException("minutes must be a factor of 60");
        }

        final DateTime hour = dateTime.hourOfDay().roundFloorCopy();
        final long millisSinceHour = new Duration(hour, dateTime).getMillis();
        final int roundedMinutes = ((int)Math.floor(
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

    public Object[] getSoonestTime() {
        if(mDate.isAfterNow()){
            for(int i=mReminders.size()-1;i>=0;i--){
                Reminder r = mReminders.get(i);
                DateTime time = mDate.minusMinutes((int)r.getMinutes());
                if(time.isAfterNow()){
                    Object[] objects = new Object[2];
                    objects[0] = time;
                    objects[1] = r;
                    return objects;
                }
            }
        }
        return null;
    }

    public boolean isOverdue() {
        return mDate.isBeforeNow();
    }

    public void setSnoozeTime(DateTime date) {
        mSnoozeTime = date;
    }

    public DateTime getSnoozeTime() {
        return mSnoozeTime;
    }
}
