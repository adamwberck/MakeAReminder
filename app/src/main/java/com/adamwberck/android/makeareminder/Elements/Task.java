package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;

import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.Service.ReminderService;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.security.acl.Group;
import java.util.List;
import java.util.Locale;

public class Task implements Serializable{

    //TODO add start date

    //TODO add exceptions (meaning on tuesday go off at a different time

    //TODO add unnamed tasks;
    private int mID;
    private String mName = "";
    //TODO split into LocalTime and LocalDate
    private DateTime mDate;
    private Repeat mRepeat;
    private DateTime mSnoozeTime;
    private boolean mHasRepeat = false;
    private boolean mComplete = false;
    private List<Reminder> mReminders = new SortedObjectList<>(10,Reminder.getComparator());

    public void addReminder(Reminder r){
        mReminders.add(r);
        GroupLab.saveLab();
    }

    public Repeat getRepeat() {
        return mRepeat;
    }

    public void setRepeat(Repeat repeat) {
        mRepeat = repeat;
        if(repeat!=null) {
            mHasRepeat = true;
        }
        else {
            mHasRepeat = false;
        }
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
        GroupLab.saveLab();
    }

    public DateTime getDate() {
        return mDate;
    }

    public void setDate(DateTime date) {
        if(date!=null) {
            this.mDate = SpanOfTime.floorDate(date,1);
            addReminder(new Reminder(this, SpanOfTime.ofMinutes(0)));
        }
        mDate = date;
        String s = mDate.toString("hh:mm a", Locale.getDefault());
        GroupLab.saveLab();
    }

    public void test(){
        setDate(new DateTime().plusMinutes(1));
        //addReminder(new Reminder(this,SpanOfTime.ofMinutes(2),true));
        String id = Math.abs(mID)+"";
        setRepeat(new Repeat(1,SpanOfTime.ofDays(1)));
        //id = id.substring(Math.min(0,id.length()-7));
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
        ReminderService.setServiceAlarm(appContext,mID,mName,false);
        ReminderService.setServiceAlarm(appContext,mID,mName,true);
    }


    public int getID() {
        return mID;
    }

    public Task(Context appContext) {
        mID = GroupLab.get(appContext).nextValue();
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
        return mDate!=null&&mDate.isBeforeNow();
    }

    public void setSnoozeTime(DateTime date) {
        mSnoozeTime = date;
    }

    public DateTime getSnoozeTime() {
        return mSnoozeTime;
    }

    public boolean hasRepeat() {
        return mHasRepeat;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
        if(mComplete){
            mSnoozeTime = null;
        }
    }

    public boolean isComplete() {
        return mComplete;
    }

    public void applyRepeat() {
        if(!isOverdue()||!hasRepeat()){
            return;
        }
        Repeat r = mRepeat;
        setComplete(false);
        SpanOfTime.Type type = r.getTimeType();
        int rawPeriod = (int) r.getRawPeriod();
        switch (type) {
            case DAY:
                while(isOverdue()) {
                    mDate = mDate.plusDays(rawPeriod);
                }
                if(r.isMoreOften()){
                    LocalTime lt = r.getTimes().get(0);
                    int hour = lt.getHourOfDay();
                    int min = lt.getMinuteOfHour();
                    mDate = new DateTime(mDate.getYear(),mDate.getMonthOfYear(),
                            mDate.getDayOfMonth(),hour,min);
                }
                return;
            case WEEK:
                mDate = mDate.plusWeeks(rawPeriod-1);
                List<Integer> weeks = r.getDayOfWeekNumbers();
                int dayOWeek = mDate.getDayOfWeek();
                while(!weeks.contains(dayOWeek)){
                    mDate = mDate.plusDays(1);
                    dayOWeek = mDate.getDayOfWeek();
                }
                while (isOverdue()){
                    mDate = mDate.plusWeeks(rawPeriod);
                }
                return;
            case MONTH:
                int month = mDate.getMonthOfYear();
                int year = mDate.getYear();
                int day = mDate.getDayOfMonth();
                int hour = mDate.getHourOfDay();
                int minute = mDate.getMinuteOfHour();

                for(int dayM : r.getMonthDays()){
                    if(day<dayM){
                        mDate = new DateTime(year,month,dayM,hour,minute);
                        while (isOverdue()){
                            mDate = mDate.plusMonths(rawPeriod);
                        }
                        return;
                    }
                }
                mDate = mDate.plusMonths(rawPeriod);

                month = mDate.getMonthOfYear();
                year = mDate.getYear();
                hour = mDate.getHourOfDay();
                minute = mDate.getMinuteOfHour();
                mDate = new DateTime(year,month,r.getMonthDays().get(0),hour,minute);
                while (isOverdue()){
                    mDate = mDate.plusMonths(rawPeriod);
                }
        }
    }

    public boolean isDueToday() {
        DateTime today = new DateTime();
        return mDate.getDayOfYear() == today.getDayOfYear() && mDate.getYear() == today.getYear();
    }
}
