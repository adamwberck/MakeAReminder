package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.Service.ReminderService;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Task implements Serializable,Cloneable{

    //TODO add start date

    //TODO add exceptions (meaning on tuesday go off at a different time

    //TODO add unnamed tasks;
    private int mID;
    private String mName = "";
    //TODO split into LocalTime and LocalDate
    private DateTime mDate;
    private Repeat mRepeat;
    private DateTime mSnoozeTime;
    private long mQuickSnoozeTime=0;
    private boolean mComplete = false;
    private List<Reminder> mReminders
            = new SortedObjectList<>(10,Reminder.getComparator());
    private Group mGroup;
    private Reminder mBaseReminder;

    public void addReminder(Reminder r){
        mReminders.add(r);
        GroupLab.saveLab();
    }


    public Repeat getRepeat() {
        return mRepeat;
    }

    public void setRepeat(Repeat repeat) {
        mRepeat = repeat;
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
            if(mDate==null && mReminders.size()==0){
                mReminders.addAll(mGroup.getDefaultReminders());
            }
            this.mDate = SpanOfTime.floorDate(date,1);

        }
        else{
            if(mReminders.equals(mGroup.getDefaultReminders())) {
                mReminders.clear();
            }
            mDate=null;
        }

        GroupLab.saveLab();
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

    public Task(Context appContext,Group group) {
        GroupLab groupLab = GroupLab.get(appContext);
        mID = groupLab.nextValue();
        mGroup = group;
        LocalTime localTime = mGroup.getDefaultTime();
        mDate = localTime!=null ? localTime.toDateTimeToday() : null;
        mRepeat = mGroup.getDefaultRepeat();
        mQuickSnoozeTime = mGroup.getDefaultSnooze();
        if(mDate!=null) {
            mReminders.addAll(mGroup.getDefaultReminders());
        }
        //Sound sound = groupLab.getSoundPlayer().getSounds().get(0);
        Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(appContext,
                RingtoneManager.TYPE_RINGTONE);
        mBaseReminder = new Reminder(this,SpanOfTime.ofMinutes(0),0
                ,false,true,defaultRingtoneUri,.5f);
    }

    @Override
    public boolean equals(Object obj) {
        //TODO change hashcode
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Task other = (Task) obj;
        return  mID == other.mID &&
                Objects.equals(mComplete, other.mComplete) &&
                Objects.equals(mName, other.mName) &&
                Objects.equals(mDate, other.mDate) &&
                Objects.equals(mRepeat, other.mRepeat) &&
                Objects.equals(mSnoozeTime, other.mSnoozeTime) &&
                Objects.equals(mQuickSnoozeTime, other.mQuickSnoozeTime) &&
                Objects.equals(mReminders, other.mReminders) &&
                Objects.equals(mGroup, other.mGroup);
    }

    public List<Reminder> getReminders() {
        return  mReminders;
    }


    public Object[] getSoonestTime() {
        if(mDate!=null&&mDate.isAfterNow()){
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
        mSnoozeTime = SpanOfTime.floorDate(date,1);
    }

    public DateTime getSnoozeTime() {
        return mSnoozeTime;
    }

    public boolean hasRepeat() {
        return mRepeat!=null;
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
        return mDate != null
                && mDate.getDayOfYear() == today.getDayOfYear()
                && mDate.getYear() == today.getYear();
    }

    public Group getGroup() {
        return mGroup;
    }

    public Task clone() throws CloneNotSupportedException {
        return (Task) super.clone();
    }

    public void setQuickSnoozeTime(long quickSnooze) {
        mQuickSnoozeTime = quickSnooze;
    }

    public long getQuickSnoozeTime() {
        return mQuickSnoozeTime;
    }

    public void toggleComplete() {
        mComplete=!mComplete;
    }

    public void setGroup(Group newGroup) {
        mGroup=newGroup;
    }

    public Reminder getBaseReminder() {
        return mBaseReminder;
    }

    public void setBaseReminder(Reminder baseReminder) {
        mBaseReminder = baseReminder;
    }
}
