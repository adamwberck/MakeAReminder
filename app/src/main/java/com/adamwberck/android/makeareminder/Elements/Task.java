package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;

import com.adamwberck.android.makeareminder.ReminderService;
import com.adamwberck.android.makeareminder.SortedReminderList;
import com.adamwberck.android.makeareminder.TaskLab;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Task implements Serializable{

    private UUID mID;
    private String mName;
    private Date mDate;
    private SpanOfTime mRepeat;
    private boolean mHasRepeat = false;
    private List<Reminder> mReminders = new SortedReminderList<>(10,Reminder.getComparator());

    public void addReminder(Reminder r){
        mReminders.add(r);
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

    public void setName(String mName) {
        this.mName = mName;
        TaskLab.saveLab();
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate,Context appContext) {
        this.mDate = mDate;
        addReminder(new Reminder(this,SpanOfTime.ofMinutes(0)));
        ReminderService.setServiceAlarm(appContext,false,this);
        TaskLab.saveLab();
    }


    public UUID getID() {
        return mID;
    }

    public Task(Context context) {
        this(UUID.randomUUID(),context);
    }

    public Task(UUID uuid,Context appContext) {
        mID = uuid;
        DateTime dtOrg = new DateTime(new Date());
        setDate(dtOrg.plusMinutes(1).toDate(),appContext);
    }

    @Override
    public boolean equals(Object o) {
            Task t = (Task) o;
            return mID.equals(t.getID());
    }

    public List<Reminder> getReminders() {
        return  mReminders;
    }
}
