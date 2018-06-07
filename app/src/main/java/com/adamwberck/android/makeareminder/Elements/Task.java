package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;

import com.adamwberck.android.makeareminder.SortedReminderList;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Task {
    private Context mContext;

    private UUID mID;
    private String mName;
    private Date mDate;
    private List<Reminder> mReminders =
            new SortedReminderList<>(10,Reminder.getComparator());

    public void addReminder(Reminder r){
        mReminders.add(r);
    }

    public void addReminder(SpanOfTime span){
        mReminders.add(new Reminder(this,span,mContext));
    }

    public void removeReminder(Reminder r){
        mReminders.remove(r);
    }

    public BigInteger getRepeat() {
        return mRepeat;
    }

    public void setRepeat(BigInteger repeat) {
        mRepeat = repeat;
    }

    private BigInteger mRepeat;

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }


    public UUID getID() {
        return mID;
    }

    public Task(Context context) {
        this(UUID.randomUUID(),context);
    }

    public Task(UUID uuid,Context context) {
        mContext = context;
        mID = uuid;
        mDate = new Date();
    }

    public boolean equals(Task r) {
        return this.mID.equals(r);
    }

    public List<Reminder> getReminders() {
        return  mReminders;
    }
}
