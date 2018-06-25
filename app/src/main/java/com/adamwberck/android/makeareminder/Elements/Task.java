package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;

import com.adamwberck.android.makeareminder.ReminderService;
import com.adamwberck.android.makeareminder.SortedObjectList;
import com.adamwberck.android.makeareminder.TaskLab;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Task implements Serializable{

    private int mID;
    private String mName = "";
    private Date mDate;
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

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate,Context appContext) {
        this.mDate = mDate;
        addReminder(new Reminder(this,SpanOfTime.ofMinutes(0)));
        TaskLab.saveLab();
        startAlarm(appContext);
    }

    public void startAlarm(Context appContext){
        if(mName.isEmpty()){
            return;
        }
        if(mDate==null){
            return;
        }
        Date now = new Date();
        if(mDate.before(now)){
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
        DateTime dtOrg = new DateTime(new Date());
        setDate(dtOrg.plusMinutes(1).toDate(),appContext);
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

}
